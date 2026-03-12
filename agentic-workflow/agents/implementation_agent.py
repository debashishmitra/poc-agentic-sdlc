"""
Implementation Agent - generates implementation code and unit tests using Claude.
Creates Java source files and corresponding test files based on the design document.
Follows a layered generation approach: DTO → Repository → Service → Controller → Unit Tests → Integration Tests.
"""

import re
from typing import Dict, Any

from utils.claude_client import ClaudeClient
from utils.formatting import print_agent_header, print_step, print_success, print_error
from utils.github_client import GitHubClient

# Base package path for the THD Order Management Service
BASE_PACKAGE = "com.thd.ordermanagement"
BASE_SRC_PATH = "src/main/java/com/thd/ordermanagement"
BASE_TEST_PATH = "src/test/java/com/thd/ordermanagement"


class ImplementationAgent:
    """Agent for generating implementation code and unit tests."""

    def __init__(self):
        """Initialize the Implementation Agent."""
        self.github = GitHubClient()
        self.claude = ClaudeClient()

    def read_repo_patterns(self) -> Dict[str, str]:
        """
        Read existing code to understand patterns used in the project.
        Reads real files from the THD Order Management Service repo.

        Returns:
            Dictionary of filename -> file content
        """
        context_files = {}
        files_to_read = [
            "pom.xml",
            f"{BASE_SRC_PATH}/model/Order.java",
            f"{BASE_SRC_PATH}/model/OrderItem.java",
            f"{BASE_SRC_PATH}/model/OrderStatus.java",
            f"{BASE_SRC_PATH}/repository/OrderRepository.java",
            f"{BASE_SRC_PATH}/service/OrderService.java",
            f"{BASE_SRC_PATH}/service/OrderServiceImpl.java",
            f"{BASE_SRC_PATH}/controller/OrderController.java",
            f"{BASE_SRC_PATH}/dto/OrderResponse.java",
            f"{BASE_SRC_PATH}/dto/CreateOrderRequest.java",
            f"{BASE_SRC_PATH}/exception/GlobalExceptionHandler.java",
            # Existing test patterns - crucial for generating matching tests
            f"{BASE_TEST_PATH}/service/OrderServiceImplTest.java",
            f"{BASE_TEST_PATH}/controller/OrderControllerTest.java",
        ]

        for file_path in files_to_read:
            try:
                file_data = self.github.get_file_content(file_path)
                if file_data:
                    import base64
                    content = base64.b64decode(file_data.get("content", "")).decode()
                    context_files[file_path] = content
            except Exception:
                pass

        return context_files

    def generate_implementation(
        self, story_data: Dict[str, Any], design_content: str
    ) -> Dict[str, str]:
        """
        Generate implementation code and unit tests using Claude.
        Follows a layered approach where each layer builds on previous ones,
        culminating in comprehensive unit and integration tests.

        Args:
            story_data: Parsed story data
            design_content: Technical design document

        Returns:
            Dictionary of code_type -> code content
        """
        print_agent_header(
            "IMPLEMENTATION AGENT", f"Generating code for story #{story_data['number']}"
        )

        try:
            print_step("Reading repository code patterns...")
            repo_patterns = self.read_repo_patterns()

            print_step("Preparing implementation prompts...")
            system_prompt = self._build_system_prompt(repo_patterns)

            implementation_code = {}

            # --- Phase 1: DTOs ---
            print_step("Generating DTO classes...")
            dto_code = self._strip_markdown_fences(self.claude.generate(
                system_prompt,
                self._build_dto_prompt(story_data, design_content, repo_patterns),
                max_tokens=4000,
            ))
            implementation_code["dto"] = dto_code

            # --- Phase 2: Repository methods ---
            print_step("Generating repository class...")
            repo_code = self._strip_markdown_fences(self.claude.generate(
                system_prompt,
                self._build_repository_prompt(
                    story_data, design_content, repo_patterns, dto_code
                ),
                max_tokens=2000,
            ))
            implementation_code["repository"] = repo_code

            # --- Phase 3: Service layer ---
            print_step("Generating service class...")
            service_code = self._strip_markdown_fences(self.claude.generate(
                system_prompt,
                self._build_service_prompt(
                    story_data, design_content, repo_patterns, dto_code, repo_code
                ),
                max_tokens=4000,
            ))
            implementation_code["service"] = service_code

            # --- Phase 4: Controller ---
            print_step("Generating controller class...")
            controller_code = self._strip_markdown_fences(self.claude.generate(
                system_prompt,
                self._build_controller_prompt(
                    story_data, design_content, repo_patterns, service_code, dto_code
                ),
                max_tokens=3000,
            ))
            implementation_code["controller"] = controller_code

            # --- Phase 5: Unit Tests (service layer) ---
            print_step("Generating service unit tests...")
            unit_tests = self._strip_markdown_fences(self.claude.generate(
                system_prompt,
                self._build_unit_test_prompt(
                    story_data, design_content, repo_patterns,
                    service_code, repo_code, dto_code
                ),
                max_tokens=6000,
            ))
            implementation_code["unit_tests"] = unit_tests

            # --- Phase 6: Controller / Integration Tests ---
            print_step("Generating controller tests...")
            integration_tests = self._strip_markdown_fences(self.claude.generate(
                system_prompt,
                self._build_controller_test_prompt(
                    story_data, design_content, repo_patterns,
                    controller_code, service_code, dto_code
                ),
                max_tokens=6000,
            ))
            implementation_code["integration_tests"] = integration_tests

            print_success(
                f"Implementation code generated: "
                f"{len(implementation_code)} components (incl. unit & controller tests)"
            )
            return implementation_code

        except Exception as e:
            print_error(f"Failed to generate implementation: {str(e)}")
            raise

    # ---------------------------------------------------------------
    # System prompt
    # ---------------------------------------------------------------

    def _build_system_prompt(self, repo_patterns: Dict[str, str]) -> str:
        """Build a project-aware system prompt for code generation."""

        # Extract existing test style if available
        existing_service_test = repo_patterns.get(
            f"{BASE_TEST_PATH}/service/OrderServiceImplTest.java", ""
        )
        existing_controller_test = repo_patterns.get(
            f"{BASE_TEST_PATH}/controller/OrderControllerTest.java", ""
        )

        test_style_section = ""
        if existing_service_test or existing_controller_test:
            test_style_section = "\n## Existing Test Patterns (MATCH THIS STYLE)\n"
            if existing_service_test:
                test_style_section += (
                    f"### Service Test Pattern (first 120 lines):\n"
                    f"```java\n{self._first_n_lines(existing_service_test, 120)}\n```\n\n"
                )
            if existing_controller_test:
                test_style_section += (
                    f"### Controller Test Pattern (first 120 lines):\n"
                    f"```java\n{self._first_n_lines(existing_controller_test, 120)}\n```\n\n"
                )

        return f"""You are an expert Java/Spring Boot developer working on the THD Order Management Service.

## Project Details
- Package: {BASE_PACKAGE}
- Framework: Spring Boot 4.0.3 with Java 25
- Namespace: Jakarta EE (jakarta.persistence, jakarta.validation)
- Jackson: Jackson 3 (tools.jackson.databind, NOT com.fasterxml.jackson)
- Testing: JUnit 5 + Mockito (use @MockitoBean NOT @MockBean for Spring Boot 4)
  - Service tests: Pure Mockito (@ExtendWith(MockitoExtension.class), @Mock, @InjectMocks)
  - Controller tests: @WebMvcTest with import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
  - MockitoBean import: org.springframework.test.context.bean.override.mockito.MockitoBean
- Existing entities: Order, OrderItem, OrderStatus (enum)
- Existing DTOs: OrderResponse, OrderItemResponse, CreateOrderRequest, OrderItemRequest, UpdateOrderStatusRequest

## Code Requirements
1. Follow existing project patterns exactly (package names, coding style, annotations)
2. Use Jakarta EE annotations (jakarta.persistence.*, jakarta.validation.*)
3. Use dependency injection via constructor or @Autowired
4. Include proper error handling and validation
5. Use BigDecimal for monetary values
6. Do NOT use Lombok — write explicit getters/setters/constructors
7. Always generate complete, compilable code with ALL necessary imports
8. When generating tests, cover happy path AND error/edge cases
9. Every public method in the service layer MUST have corresponding unit tests
{test_style_section}
IMPORTANT: Output ONLY raw Java code starting with the package declaration. No markdown fences, no explanations."""

    # ---------------------------------------------------------------
    # DTO prompt
    # ---------------------------------------------------------------

    def _build_dto_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
    ) -> str:
        """Build prompt for DTO generation."""
        existing_dto = repo_patterns.get(
            f"{BASE_SRC_PATH}/dto/OrderResponse.java", "No existing DTOs found"
        )

        return f"""Based on the design document below, generate any NEW DTO classes needed.

## Design Document
{design_content[:3000]}

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

## Existing DTO Pattern (follow this style)
{existing_dto[:800]}

Generate DTO class(es) in package {BASE_PACKAGE}.dto that:
1. Follow the existing DTO patterns (no Lombok, explicit getters/setters)
2. Include all fields needed by the new endpoint(s)
3. Use appropriate field types (BigDecimal for money, Long for counts, etc.)
4. Include a default constructor and an all-args constructor

If multiple DTOs are needed, include them all in one output separated by a clear comment:
// === NEW FILE: ClassName.java ===

Only output Java code starting with package declaration."""

    # ---------------------------------------------------------------
    # Repository prompt
    # ---------------------------------------------------------------

    def _build_repository_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        dto_code: str,
    ) -> str:
        """Build prompt for repository generation."""
        existing_repo = repo_patterns.get(
            f"{BASE_SRC_PATH}/repository/OrderRepository.java",
            "No existing repositories found",
        )

        return f"""Based on the design document, generate repository additions or a new repository interface.

## Design Document (relevant sections)
{design_content[:2000]}

## Generated DTOs
{dto_code[:1500]}

## Existing Repository (extend or add methods to this)
{existing_repo[:1000]}

## Story Information
Story #{story_data['number']}: {story_data['title']}

If the existing OrderRepository already covers the need, generate ONLY the new query methods
that need to be added (with a comment explaining where they go).

If a brand-new repository is needed, generate the full interface in package {BASE_PACKAGE}.repository.

Only output Java code starting with package declaration."""

    # ---------------------------------------------------------------
    # Service prompt
    # ---------------------------------------------------------------

    def _build_service_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        dto_code: str,
        repo_code: str,
    ) -> str:
        """Build prompt for service generation."""
        existing_service = repo_patterns.get(
            f"{BASE_SRC_PATH}/service/OrderServiceImpl.java",
            "No existing services found",
        )
        existing_interface = repo_patterns.get(
            f"{BASE_SRC_PATH}/service/OrderService.java", ""
        )

        return f"""Based on the design document, generate the service layer changes.

## Design Document
{design_content[:2500]}

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

## Generated DTOs
{dto_code[:1500]}

## Generated/Modified Repository
{repo_code[:1000]}

## Existing Service Interface
{existing_interface[:800]}

## Existing Service Implementation (follow this pattern)
{existing_service[:1500]}

Generate both the interface additions AND the implementation.
Put them in a single output separated by:
// === NEW FILE: ServiceInterface.java ===
// === NEW FILE: ServiceImpl.java ===

The service must:
1. Be in package {BASE_PACKAGE}.service
2. Follow the existing service pattern (interface + impl)
3. Include @Service and @Transactional annotations where appropriate
4. Convert between entities and DTOs
5. Include proper error handling (throw appropriate exceptions)
6. Include logging via SLF4J

Only output Java code starting with package declaration."""

    # ---------------------------------------------------------------
    # Controller prompt
    # ---------------------------------------------------------------

    def _build_controller_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        service_code: str,
        dto_code: str,
    ) -> str:
        """Build prompt for controller generation."""
        existing_controller = repo_patterns.get(
            f"{BASE_SRC_PATH}/controller/OrderController.java",
            "No existing controllers found",
        )

        return f"""Based on the design document, generate the REST controller changes.

## Design Document (API Specifications)
{design_content[:3000]}

## Story Information
Story #{story_data['number']}: {story_data['title']}

## Generated Service
{service_code[:2000]}

## Generated DTOs
{dto_code[:1000]}

## Existing Controller (add new endpoints following this pattern)
{existing_controller[:2000]}

IMPORTANT: The existing OrderController at /api/v1/orders already exists.
You should generate ONLY the new endpoint method(s) that need to be ADDED to it,
with a comment like:
// --- Add the following method(s) to OrderController.java ---

Or, if the design calls for a completely new controller, generate the full class
in package {BASE_PACKAGE}.controller.

The controller must:
1. Use @RestController and @RequestMapping
2. Follow the existing controller pattern exactly
3. Include proper @Operation annotations for Swagger/OpenAPI
4. Use ResponseEntity for responses
5. Include input validation where applicable
6. Include logging

Only output Java code starting with package declaration or with the comment marker."""

    # ---------------------------------------------------------------
    # Unit Test prompt (Service layer)
    # ---------------------------------------------------------------

    def _build_unit_test_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        service_code: str,
        repo_code: str,
        dto_code: str,
    ) -> str:
        """
        Build prompt for comprehensive unit tests of the service layer.
        Uses existing test patterns to ensure generated tests match the project style.
        """
        existing_test = repo_patterns.get(
            f"{BASE_TEST_PATH}/service/OrderServiceImplTest.java", ""
        )

        return f"""Generate comprehensive unit tests for the NEW service methods.

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

## Service Code to Test
{service_code[:3000]}

## Repository (dependencies to mock)
{repo_code[:1500]}

## DTOs Used
{dto_code[:1500]}

## EXISTING Test Pattern (you MUST match this style exactly)
{self._first_n_lines(existing_test, 150) if existing_test else "No existing tests found — use standard JUnit 5 + Mockito style"}

## Test Requirements
Generate a test class in package {BASE_PACKAGE}.service that:

1. Uses @ExtendWith(MockitoExtension.class) — NOT @SpringBootTest
2. Uses @Mock for repository dependencies
3. Uses @InjectMocks for the service implementation under test
4. Tests EVERY public method in the new/modified service
5. For each method, include tests for:
   - Happy path (normal successful execution)
   - Edge cases (empty results, boundary values)
   - Error cases (exceptions, invalid input)
6. Uses descriptive test method names: should_expectedResult_when_condition()
7. Follows Arrange-Act-Assert pattern with clear sections
8. Uses Mockito.when() / verify() for mocking
9. Uses assertEquals, assertNotNull, assertThrows from JUnit 5
10. Targets ≥90% code coverage of the service methods

IMPORTANT: Do NOT use @MockBean or @WebMvcTest — these are pure unit tests with Mockito only.

Only output the Java test code starting with package declaration."""

    # ---------------------------------------------------------------
    # Controller Test prompt
    # ---------------------------------------------------------------

    def _build_controller_test_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        controller_code: str,
        service_code: str,
        dto_code: str,
    ) -> str:
        """
        Build prompt for controller/integration tests using @WebMvcTest.
        Uses existing controller test patterns to ensure consistency.
        """
        existing_test = repo_patterns.get(
            f"{BASE_TEST_PATH}/controller/OrderControllerTest.java", ""
        )

        return f"""Generate controller tests for the NEW endpoint(s).

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

## Controller Code to Test
{controller_code[:2500]}

## Service (dependency to mock)
{service_code[:1500]}

## DTOs
{dto_code[:1500]}

## EXISTING Controller Test Pattern (you MUST match this style exactly)
{self._first_n_lines(existing_test, 150) if existing_test else "No existing controller tests found"}

## Test Requirements
Generate a test class in package {BASE_PACKAGE}.controller that:

1. Uses @WebMvcTest annotation — import from org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
2. Uses @MockitoBean (NOT @MockBean) — import from org.springframework.test.context.bean.override.mockito.MockitoBean
3. Uses @Autowired MockMvc for performing requests
4. Uses @Autowired ObjectMapper (tools.jackson.databind.ObjectMapper) for JSON serialization
5. Tests EVERY new endpoint for:
   - Successful response (HTTP 200/201)
   - Proper JSON response body structure
   - Error scenarios (404, 400, 500)
   - Input validation if applicable
6. Uses mockMvc.perform(get/post/put/delete(...))
7. Uses .andExpect(status().isOk()) style assertions
8. Uses .andExpect(jsonPath("$.field").value(...)) for response body checks
9. Follows the existing test naming convention
10. Mocks the service layer appropriately with when().thenReturn() / thenThrow()

IMPORTANT:
- Import @WebMvcTest from org.springframework.boot.webmvc.test.autoconfigure
- Import ObjectMapper from tools.jackson.databind (NOT com.fasterxml.jackson)
- Import @MockitoBean from org.springframework.test.context.bean.override.mockito

Only output the Java test code starting with package declaration."""

    # ---------------------------------------------------------------
    # Helpers
    # ---------------------------------------------------------------

    @staticmethod
    def _first_n_lines(text: str, n: int) -> str:
        """Return the first n lines of text."""
        lines = text.split("\n")
        return "\n".join(lines[:n])

    @staticmethod
    def _strip_markdown_fences(code: str) -> str:
        """
        Strip markdown code fences from Claude-generated code.

        Claude often wraps responses in ```java ... ``` even when asked not to.
        This method removes those fences so the committed .java files are compilable.
        It also strips any leading/trailing prose that appears before the first
        'package' declaration or after the last closing brace.
        """
        if not code:
            return code

        text = code.strip()

        # Remove opening fences like ```java, ```Java, ```
        text = re.sub(r"^```(?:java|Java|JAVA)?\s*\n?", "", text)
        # Remove closing fences
        text = re.sub(r"\n?```\s*$", "", text)

        # If there are multiple code blocks (e.g. explanation + code + explanation),
        # extract everything between the first 'package' and the last '}'
        # but only if a package declaration is present.
        pkg_match = re.search(r"^(package\s+[\w.]+;)", text, re.MULTILINE)
        if pkg_match:
            start_idx = pkg_match.start()
            # Find the last closing brace
            last_brace = text.rfind("}")
            if last_brace > start_idx:
                text = text[start_idx : last_brace + 1]

        return text.strip()

    # ---------------------------------------------------------------
    # Extract class names and file paths from generated code
    # ---------------------------------------------------------------

    def extract_classes_from_code(
        self, implementation_code: Dict[str, str]
    ) -> Dict[str, list]:
        """
        Extract class names and suggested file paths from generated code.
        Supports multiple classes per code_type (separated by // === NEW FILE: ... ===).

        Args:
            implementation_code: Generated code dictionary

        Returns:
            Dictionary mapping code_type to list of (class_name, file_path, code_content)
        """
        files = {}

        for code_type, code_content in implementation_code.items():
            # Split on "// === NEW FILE:" markers if present
            segments = re.split(r"//\s*===\s*NEW FILE:\s*\w+\.java\s*===", code_content)
            # If no split happened, treat the whole content as one segment
            if len(segments) == 1:
                segments = [code_content]
            else:
                # The first segment might be empty or contain the first class
                segments = [s.strip() for s in segments if s.strip()]

            file_list = []
            for segment in segments:
                # Clean any remaining markdown fences in individual segments
                cleaned = self._strip_markdown_fences(segment)
                if not cleaned:
                    continue
                match = re.search(
                    r"public\s+(?:class|interface)\s+(\w+)",
                    cleaned,
                )
                if match:
                    class_name = match.group(1)
                    file_path = self._class_to_path(code_type, class_name)
                    file_list.append((class_name, file_path, cleaned))

            if file_list:
                files[code_type] = file_list

        return files

    def _class_to_path(self, code_type: str, class_name: str) -> str:
        """Map a code type + class name to the correct file path."""
        if code_type == "dto":
            return f"{BASE_SRC_PATH}/dto/{class_name}.java"
        elif code_type == "entity":
            return f"{BASE_SRC_PATH}/model/{class_name}.java"
        elif code_type == "repository":
            return f"{BASE_SRC_PATH}/repository/{class_name}.java"
        elif code_type == "service":
            if "Impl" in class_name:
                return f"{BASE_SRC_PATH}/service/{class_name}.java"
            elif "Service" in class_name:
                return f"{BASE_SRC_PATH}/service/{class_name}.java"
            else:
                return f"{BASE_SRC_PATH}/service/{class_name}.java"
        elif code_type == "controller":
            return f"{BASE_SRC_PATH}/controller/{class_name}.java"
        elif code_type == "unit_tests":
            return f"{BASE_TEST_PATH}/service/{class_name}.java"
        elif code_type == "integration_tests":
            return f"{BASE_TEST_PATH}/controller/{class_name}.java"
        else:
            return f"{BASE_SRC_PATH}/{code_type}/{class_name}.java"

    # ---------------------------------------------------------------
    # Commit implementation to GitHub
    # ---------------------------------------------------------------

    def commit_implementation(
        self, implementation_code: Dict[str, str], story_number: int
    ) -> Dict[str, Any]:
        """
        Commit implementation code to a new branch.

        Args:
            implementation_code: Generated code dictionary
            story_number: Story number

        Returns:
            Commit results
        """
        print_step("Creating implementation branch...")

        branch_name = f"feature/STORY-{story_number}"
        commit_results = {}

        try:
            # Create branch
            self.github.create_branch(branch_name)
            print_step(f"Branch created: {branch_name}")

            # Extract class names and file paths (supports multi-file segments)
            files = self.extract_classes_from_code(implementation_code)

            # Commit each generated file
            for code_type, file_list in files.items():
                for class_name, file_path, code_content in file_list:
                    self.github.create_or_update_file(
                        path=file_path,
                        content=code_content,
                        message=f"Implement: {class_name} ({code_type}) for STORY-{story_number}",
                        branch=branch_name,
                    )

                    key = f"{code_type}_{class_name}"
                    commit_results[key] = {
                        "file": file_path,
                        "class": class_name,
                        "type": code_type,
                    }
                    print_success(f"Committed: {file_path}")

            return {
                "branch": branch_name,
                "files": commit_results,
                "count": len(commit_results),
            }

        except Exception as e:
            print_error(f"Failed to commit implementation: {str(e)}")
            raise

    # ---------------------------------------------------------------
    # Create PR
    # ---------------------------------------------------------------

    def create_implementation_pr(
        self, story_number: int, implementation_files: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Create a pull request for the implementation.

        Args:
            story_number: Story number
            implementation_files: Committed files information

        Returns:
            PR data
        """
        print_step("Creating implementation pull request...")

        branch_name = f"feature/STORY-{story_number}"
        pr_title = f"Feature: Implementation for STORY-{story_number}"

        # Separate source and test files for the PR body
        source_files = []
        test_files = []
        for v in implementation_files["files"].values():
            entry = f"- `{v['class']}` in `{v['file']}`"
            if v.get("type") in ("unit_tests", "integration_tests"):
                test_files.append(entry)
            else:
                source_files.append(entry)

        source_list = "\n".join(source_files) if source_files else "- (none)"
        test_list = "\n".join(test_files) if test_files else "- (none)"

        pr_body = f"""## Implementation for STORY-{story_number}

This pull request contains the implementation for story #{story_number}.

### Source Files
{source_list}

### Test Files
{test_list}

### Review Checklist
- [ ] Code follows Spring Boot best practices
- [ ] All tests pass
- [ ] Code has proper error handling
- [ ] No security vulnerabilities
- [ ] Documentation is updated

### Testing
Run tests with:
```bash
mvn test
mvn verify
```

---
*Generated by Agentic Workflow Orchestrator*
"""

        try:
            pr_data = self.github.create_pull_request(
                title=pr_title,
                body=pr_body,
                head_branch=branch_name,
                base="main",
            )

            pr_number = pr_data.get("number")
            print_success(f"Implementation PR created: #{pr_number}")

            return pr_data

        except Exception as e:
            print_error(f"Failed to create implementation PR: {str(e)}")
            raise

    # ---------------------------------------------------------------
    # Main entry point
    # ---------------------------------------------------------------

    def run(
        self, story_data: Dict[str, Any], design_content: str
    ) -> Dict[str, Any]:
        """
        Run the implementation agent end-to-end.

        Args:
            story_data: Parsed story data
            design_content: Technical design document

        Returns:
            Implementation results
        """
        # Generate implementation code + tests
        implementation_code = self.generate_implementation(story_data, design_content)

        # Commit implementation
        commit_result = self.commit_implementation(
            implementation_code, story_data["number"]
        )

        # Create implementation PR
        pr_data = self.create_implementation_pr(story_data["number"], commit_result)

        return {
            "implementation_code": implementation_code,
            "branch": commit_result["branch"],
            "files": commit_result["files"],
            "pr_number": pr_data["number"],
            "pr_url": pr_data.get("html_url"),
        }
