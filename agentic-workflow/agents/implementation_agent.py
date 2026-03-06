"""
Implementation Agent - generates implementation code using Claude.
Creates Java files, unit tests, and integration tests based on the design document.
"""

import re
from typing import Dict, Any, List
from utils.github_client import GitHubClient
from utils.claude_client import ClaudeClient
from utils.formatting import print_agent_header, print_step, print_success, print_error


class ImplementationAgent:
    """Agent for generating implementation code."""

    def __init__(self):
        """Initialize the Implementation Agent."""
        self.github = GitHubClient()
        self.claude = ClaudeClient()

    def read_repo_patterns(self) -> Dict[str, str]:
        """
        Read existing code to understand patterns.

        Returns:
            Dictionary of filename -> file content
        """
        context_files = {}
        files_to_read = [
            "pom.xml",
            "src/main/java/com/example/model/User.java",
            "src/main/java/com/example/repository/UserRepository.java",
            "src/main/java/com/example/service/UserService.java",
            "src/main/java/com/example/controller/UserController.java",
            "src/test/java/com/example/service/UserServiceTest.java",
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
        Generate implementation code using Claude.

        Args:
            story_data: Parsed story data
            design_content: Technical design document

        Returns:
            Dictionary of filename -> code content
        """
        print_agent_header(
            "IMPLEMENTATION AGENT", f"Generating code for story #{story_data['number']}"
        )

        try:
            print_step("Reading repository code patterns...")
            repo_patterns = self.read_repo_patterns()

            print_step("Preparing implementation prompts...")
            system_prompt = self._build_system_prompt()

            # Generate different components
            implementation_code = {}

            # Generate entity/model
            print_step("Generating entity models...")
            entity_code = self.claude.generate(
                system_prompt,
                self._build_entity_prompt(story_data, design_content, repo_patterns),
                max_tokens=3000,
            )
            implementation_code["entity"] = entity_code

            # Generate repository
            print_step("Generating repository class...")
            repo_code = self.claude.generate(
                system_prompt,
                self._build_repository_prompt(
                    story_data, design_content, repo_patterns, entity_code
                ),
                max_tokens=2000,
            )
            implementation_code["repository"] = repo_code

            # Generate service
            print_step("Generating service class...")
            service_code = self.claude.generate(
                system_prompt,
                self._build_service_prompt(
                    story_data, design_content, repo_patterns, entity_code, repo_code
                ),
                max_tokens=3000,
            )
            implementation_code["service"] = service_code

            # Generate controller
            print_step("Generating controller class...")
            controller_code = self.claude.generate(
                system_prompt,
                self._build_controller_prompt(
                    story_data, design_content, repo_patterns, service_code
                ),
                max_tokens=2500,
            )
            implementation_code["controller"] = controller_code

            # Generate unit tests
            print_step("Generating unit tests...")
            unit_tests = self.claude.generate(
                system_prompt,
                self._build_unit_test_prompt(story_data, service_code),
                max_tokens=3000,
            )
            implementation_code["unit_tests"] = unit_tests

            # Generate integration tests
            print_step("Generating integration tests...")
            integration_tests = self.claude.generate(
                system_prompt,
                self._build_integration_test_prompt(story_data, controller_code),
                max_tokens=3000,
            )
            implementation_code["integration_tests"] = integration_tests

            print_success("Implementation code generated successfully")
            return implementation_code

        except Exception as e:
            print_error(f"Failed to generate implementation: {str(e)}")
            raise

    def _build_system_prompt(self) -> str:
        """Build the system prompt for code generation."""
        return """You are an expert Java/Spring Boot developer.
Your task is to generate production-ready code following Spring Boot best practices.

Code requirements:
1. Follow Spring Boot conventions and best practices
2. Use appropriate annotations (@Entity, @Repository, @Service, @RestController, etc.)
3. Include proper error handling and validation
4. Use dependency injection
5. Write clear, maintainable code with comments
6. Include proper logging
7. Follow naming conventions
8. Use appropriate design patterns

Always generate complete, compilable code with all necessary imports."""

    def _build_entity_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
    ) -> str:
        """Build prompt for entity generation."""
        existing_entity = repo_patterns.get(
            "src/main/java/com/example/model/User.java", "No existing entities"
        )

        return f"""Based on the design document below, generate a JPA Entity class.

## Design Document
{design_content[:2000]}

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

## Existing Entity Pattern
{existing_entity[:500]}

Generate a JPA Entity class (@Entity) that:
1. Implements the data model from the design
2. Uses appropriate field types and annotations
3. Includes proper relationships (if any)
4. Has a constructor and getters/setters
5. Follows the existing code patterns

Only output the Java code, starting with package declaration."""

    def _build_repository_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        entity_code: str,
    ) -> str:
        """Build prompt for repository generation."""
        existing_repo = repo_patterns.get(
            "src/main/java/com/example/repository/UserRepository.java",
            "No existing repositories",
        )

        return f"""Based on the design document and entity, generate a Spring Data Repository.

## Design Document (API Specs Section)
{design_content[:2000]}

## Generated Entity
{entity_code[:1000]}

## Existing Repository Pattern
{existing_repo[:500]}

Generate a Repository interface that:
1. Extends JpaRepository or CrudRepository
2. Includes custom query methods as specified in the design
3. Uses @Query annotations if needed
4. Follows the existing patterns

Only output the Java code starting with package declaration."""

    def _build_service_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        entity_code: str,
        repo_code: str,
    ) -> str:
        """Build prompt for service generation."""
        existing_service = repo_patterns.get(
            "src/main/java/com/example/service/UserService.java",
            "No existing services",
        )

        return f"""Based on the design document, generate a Spring Service class.

## Design Document (Service Layer Section)
{design_content[:2000]}

## Existing Service Pattern
{existing_service[:500]}

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

Generate a Service class (@Service) that:
1. Contains business logic methods from the design
2. Uses dependency injection for repositories
3. Includes proper error handling and validation
4. Implements transactions where appropriate (@Transactional)
5. Has clear logging statements
6. Follows the existing code patterns

Only output the Java code starting with package declaration."""

    def _build_controller_prompt(
        self,
        story_data: Dict[str, Any],
        design_content: str,
        repo_patterns: Dict[str, str],
        service_code: str,
    ) -> str:
        """Build prompt for controller generation."""
        existing_controller = repo_patterns.get(
            "src/main/java/com/example/controller/UserController.java",
            "No existing controllers",
        )

        return f"""Based on the design document, generate a Spring REST Controller.

## Design Document (API Specifications)
{design_content[:2500]}

## Existing Controller Pattern
{existing_controller[:500]}

## Story Information
Story #{story_data['number']}: {story_data['title']}

Generate a REST Controller (@RestController) that:
1. Implements all endpoints specified in the design
2. Uses proper HTTP methods (GET, POST, PUT, DELETE)
3. Includes proper @RequestMapping annotations
4. Validates input parameters
5. Handles errors with appropriate HTTP status codes
6. Uses dependency injection for services
7. Includes logging
8. Follows the existing patterns

Only output the Java code starting with package declaration."""

    def _build_unit_test_prompt(
        self, story_data: Dict[str, Any], service_code: str
    ) -> str:
        """Build prompt for unit test generation."""
        return f"""Generate comprehensive unit tests for the service class generated above.

## Service Code
{service_code[:1500]}

## Story Information
Story #{story_data['number']}: {story_data['title']}
Acceptance Criteria:
{story_data.get('acceptance_criteria', 'N/A')}

Generate unit tests using JUnit 5 and Mockito that:
1. Test all public methods of the service
2. Cover happy path and error cases
3. Mock dependencies appropriately
4. Test business logic validation
5. Use @Mock, @InjectMocks, and @Test annotations
6. Include clear test method names

Only output the Java test code starting with package declaration."""

    def _build_integration_test_prompt(
        self, story_data: Dict[str, Any], controller_code: str
    ) -> str:
        """Build prompt for integration test generation."""
        return f"""Generate integration tests for the REST controller.

## Controller Code
{controller_code[:1500]}

## Story Information
Story #{story_data['number']}: {story_data['title']}

Generate integration tests using Spring Boot Test that:
1. Test API endpoints end-to-end
2. Use @SpringBootTest and @AutoConfigureMockMvc
3. Test various HTTP methods and status codes
4. Include error scenario testing
5. Verify response content and structure
6. Test input validation

Only output the Java test code starting with package declaration."""

    def extract_classes_from_code(
        self, implementation_code: Dict[str, str]
    ) -> Dict[str, tuple]:
        """
        Extract class names and suggested file paths from generated code.

        Args:
            implementation_code: Generated code dictionary

        Returns:
            Dictionary mapping code type to (class_name, file_path)
        """
        files = {}

        # Extract class names using regex
        for code_type, code_content in implementation_code.items():
            # Look for "public class ClassName"
            match = re.search(
                r"public\s+(?:class|interface)\s+(\w+)",
                code_content,
            )
            if match:
                class_name = match.group(1)

                # Determine file path based on code type
                if code_type == "entity":
                    file_path = f"src/main/java/com/example/model/{class_name}.java"
                elif code_type == "repository":
                    file_path = (
                        f"src/main/java/com/example/repository/{class_name}.java"
                    )
                elif code_type == "service":
                    file_path = f"src/main/java/com/example/service/{class_name}.java"
                elif code_type == "controller":
                    file_path = f"src/main/java/com/example/controller/{class_name}.java"
                elif code_type == "unit_tests":
                    file_path = f"src/test/java/com/example/service/{class_name}.java"
                elif code_type == "integration_tests":
                    file_path = f"src/test/java/com/example/controller/{class_name}.java"
                else:
                    file_path = f"src/main/java/com/example/{code_type}/{class_name}.java"

                files[code_type] = (class_name, file_path)

        return files

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

            # Extract class names and file paths
            files = self.extract_classes_from_code(implementation_code)

            # Commit each generated file
            for code_type, code_content in implementation_code.items():
                if code_type not in files:
                    continue

                class_name, file_path = files[code_type]

                # Commit the file
                self.github.create_or_update_file(
                    path=file_path,
                    content=code_content,
                    message=f"Implement: {code_type.replace('_', ' ')} for STORY-{story_number}",
                    branch=branch_name,
                )

                commit_results[code_type] = {
                    "file": file_path,
                    "class": class_name,
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

        file_list = "\n".join(
            f"- {v['class']} in `{v['file']}`"
            for v in implementation_files["files"].values()
        )

        pr_body = f"""## Implementation for STORY-{story_number}

This pull request contains the implementation for story #{story_number}.

### Changes
{file_list}

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
        # Generate implementation code
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
