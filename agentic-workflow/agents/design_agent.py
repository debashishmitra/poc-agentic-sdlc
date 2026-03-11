"""
Design Agent - generates technical design documents using Claude.
Creates comprehensive design docs including API specs, data models, and architecture.
"""

import re
from typing import Dict, Any
from utils.github_client import GitHubClient
from utils.claude_client import ClaudeClient
from utils.formatting import print_agent_header, print_step, print_success, print_error
from datetime import datetime


class DesignAgent:
    """Agent for generating technical design documents."""

    def __init__(self):
        """Initialize the Design Agent."""
        self.github = GitHubClient()
        self.claude = ClaudeClient()

    def read_repo_context(self) -> Dict[str, str]:
        """
        Read relevant files from the repository for context.

        Returns:
            Dictionary of filename -> file content
        """
        context_files = {}
        files_to_read = [
            "README.md",
            "pom.xml",
            "src/main/java/com/thd/ordermanagement/model/Order.java",
            "src/main/java/com/thd/ordermanagement/model/OrderStatus.java",
            "src/main/java/com/thd/ordermanagement/repository/OrderRepository.java",
            "src/main/java/com/thd/ordermanagement/service/OrderService.java",
            "src/main/java/com/thd/ordermanagement/service/OrderServiceImpl.java",
            "src/main/java/com/thd/ordermanagement/controller/OrderController.java",
            "src/main/java/com/thd/ordermanagement/dto/OrderResponse.java",
        ]

        for file_path in files_to_read:
            try:
                file_data = self.github.get_file_content(file_path)
                if file_data:
                    import base64

                    content = base64.b64decode(file_data.get("content", "")).decode()
                    context_files[file_path] = content
            except Exception:
                # File doesn't exist, skip
                pass

        return context_files

    def generate_design(self, story_data: Dict[str, Any]) -> str:
        """
        Generate a technical design document using Claude.

        Args:
            story_data: Parsed story data

        Returns:
            Generated design document
        """
        print_agent_header("DESIGN AGENT", f"Generating design for story #{story_data['number']}")

        try:
            print_step("Reading repository context...")
            context_files = self.read_repo_context()

            print_step("Preparing design prompt...")
            system_prompt = self._build_system_prompt()
            user_prompt = self._build_user_prompt(story_data, context_files)

            print_step("Calling Claude API to generate design...")
            design_doc = self.claude.generate(system_prompt, user_prompt, max_tokens=6000)

            print_success("Design document generated successfully")
            return design_doc

        except Exception as e:
            print_error(f"Failed to generate design: {str(e)}")
            raise

    def _build_system_prompt(self) -> str:
        """Build the system prompt for design generation."""
        return """You are an expert software architect specializing in Spring Boot microservices.
Your task is to generate comprehensive technical design documents.

Design documents should include:
1. Overview and objectives
2. API Specifications (REST endpoints with request/response schemas)
3. Data Model Changes (entity relationships, database schema)
4. Architecture Diagram (in Mermaid format)
5. Service Layer Design (methods, interactions, responsibilities)
6. Testing Strategy (unit tests, integration tests, edge cases)
7. Implementation Notes (considerations, constraints, dependencies)
8. Error Handling Strategy
9. Performance Considerations

Use clear markdown formatting with code blocks for examples and diagrams.
Focus on practical, implementable designs that follow Spring Boot best practices."""

    def _build_user_prompt(
        self, story_data: Dict[str, Any], context_files: Dict[str, str]
    ) -> str:
        """Build the user prompt with story data and context."""
        context_str = ""
        if context_files:
            context_str = "\n## Existing Code Patterns:\n"
            for filename, content in context_files.items():
                context_str += f"\n### {filename}\n```\n{content[:1000]}\n...\n```\n"

        prompt = f"""Design a technical solution for the following user story:

## User Story #{story_data['number']}
**Title:** {story_data['title']}
**Story Points:** {story_data['story_points']}
**Priority:** {story_data['priority']}

### Description
{story_data.get('description', story_data.get('body', 'N/A'))}

### Acceptance Criteria
{story_data.get('acceptance_criteria', 'N/A')}

### Technical Notes
{story_data.get('technical_notes', 'None provided')}

### Implementation Notes
{story_data.get('implementation_notes', 'None provided')}

{context_str}

Please generate a comprehensive technical design document that will guide the implementation team."""

        return prompt

    def commit_design_doc(
        self, design_content: str, story_number: int
    ) -> Dict[str, Any]:
        """
        Commit the design document to a new branch.

        Args:
            design_content: The generated design document
            story_number: Story number for branch naming

        Returns:
            Response from GitHub API
        """
        print_step("Creating design branch...")

        branch_name = f"design/STORY-{story_number}"
        file_path = f"docs/designs/Design-STORY-{story_number}.md"

        # Add header and metadata to the design doc
        full_content = f"""# Technical Design Document
**Story:** STORY-{story_number}
**Generated:** {datetime.now().isoformat()}
**Status:** In Review

---

{design_content}

---

## Review Checklist
- [ ] API specifications are clear and complete
- [ ] Data model changes are well-defined
- [ ] Architecture diagrams are accurate
- [ ] Testing strategy is comprehensive
- [ ] Implementation is feasible within estimated points
"""

        try:
            # Create branch
            self.github.create_branch(branch_name)
            print_step(f"Branch created: {branch_name}")

            # Commit the design document
            self.github.create_or_update_file(
                path=file_path,
                content=full_content,
                message=f"Design: Technical design document for STORY-{story_number}",
                branch=branch_name,
            )
            print_success(f"Design document committed to {file_path}")

            return {"branch": branch_name, "file": file_path}

        except Exception as e:
            print_error(f"Failed to commit design document: {str(e)}")
            raise

    def create_design_pr(
        self, story_number: int, design_content: str
    ) -> Dict[str, Any]:
        """
        Create a pull request for the design document.

        Args:
            story_number: Story number
            design_content: Design document content

        Returns:
            PR data
        """
        print_step("Creating design review pull request...")

        branch_name = f"design/STORY-{story_number}"
        pr_title = f"Design: Technical design for STORY-{story_number}"
        pr_body = f"""## Design Review for STORY-{story_number}

This pull request contains the technical design document for story #{story_number}.

### Review Checklist
- [ ] Design follows architectural guidelines
- [ ] APIs are well-specified
- [ ] Data model changes are clear
- [ ] Testing strategy is comprehensive
- [ ] Implementation is feasible

### Next Steps
Once approved, the implementation team will proceed with the implementation phase.

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
            print_success(f"Design PR created: #{pr_number}")

            # Add label
            try:
                self.github.update_issue_labels(story_number, ["design-review"])
            except Exception:
                pass

            return pr_data

        except Exception as e:
            print_error(f"Failed to create design PR: {str(e)}")
            raise

    def add_issue_comment(self, issue_number: int, pr_number: int):
        """
        Add a comment to the original issue linking to the design PR.

        Args:
            issue_number: Issue number
            pr_number: PR number
        """
        print_step("Adding design link comment to issue...")

        comment = f"""## Design Document Ready
The technical design document for this story has been generated and is ready for review.

Review the design in PR #{pr_number}: https://github.com/debashishmitra/poc-agentic-sdlc/pull/{pr_number}

Once the design is approved, implementation will proceed.
"""

        try:
            self.github.add_issue_comment(issue_number, comment)
            print_success("Comment added to issue")
        except Exception as e:
            print_error(f"Failed to add comment: {str(e)}")

    def run(self, story_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Run the design agent end-to-end.

        Args:
            story_data: Parsed story data

        Returns:
            Design agent results
        """
        # Generate design
        design_content = self.generate_design(story_data)

        # Commit design document
        commit_result = self.commit_design_doc(design_content, story_data["number"])

        # Create design PR
        pr_data = self.create_design_pr(story_data["number"], design_content)

        # Add comment to original issue
        self.add_issue_comment(story_data["number"], pr_data["number"])

        return {
            "design_content": design_content,
            "branch": commit_result["branch"],
            "file": commit_result["file"],
            "pr_number": pr_data["number"],
            "pr_url": pr_data.get("html_url"),
        }
