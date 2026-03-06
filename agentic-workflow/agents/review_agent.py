"""
Review Agent - performs automated code review using Claude.
Reviews pull requests and provides feedback on code quality, patterns, and best practices.
"""

import re
from typing import Dict, Any, List
from utils.github_client import GitHubClient
from utils.claude_client import ClaudeClient
from utils.formatting import print_agent_header, print_step, print_success, print_error


class ReviewAgent:
    """Agent for performing automated code review."""

    def __init__(self):
        """Initialize the Review Agent."""
        self.github = GitHubClient()
        self.claude = ClaudeClient()

    def get_pr_context(self, pr_number: int) -> Dict[str, Any]:
        """
        Get PR and diff information.

        Args:
            pr_number: PR number

        Returns:
            PR context including diff
        """
        print_step("Fetching PR information...")

        try:
            pr_data = self.github.get_pr(pr_number)
            pr_diff = self.github.get_pr_diff(pr_number)

            return {
                "pr_data": pr_data,
                "pr_diff": pr_diff,
                "title": pr_data.get("title"),
                "body": pr_data.get("body", ""),
                "head_branch": pr_data.get("head", {}).get("ref"),
            }
        except Exception as e:
            print_error(f"Failed to fetch PR context: {str(e)}")
            raise

    def analyze_pr(self, pr_context: Dict[str, Any]) -> str:
        """
        Analyze PR code using Claude.

        Args:
            pr_context: PR information and diff

        Returns:
            Code review analysis
        """
        print_step("Analyzing code with Claude...")

        system_prompt = self._build_review_system_prompt()
        user_prompt = self._build_review_prompt(pr_context)

        try:
            review = self.claude.generate(
                system_prompt, user_prompt, max_tokens=4000
            )
            return review
        except Exception as e:
            print_error(f"Failed to analyze PR: {str(e)}")
            raise

    def _build_review_system_prompt(self) -> str:
        """Build the system prompt for code review."""
        return """You are an expert code reviewer specializing in Spring Boot applications.
Your task is to review pull requests and provide constructive feedback.

Review areas:
1. Code Quality - clarity, maintainability, complexity
2. Error Handling - exception handling, validation
3. Testing - test coverage, test quality
4. Security - potential vulnerabilities, input validation
5. Performance - N+1 queries, inefficient patterns
6. Spring Boot Patterns - proper use of annotations, best practices
7. Java Standards - naming conventions, SOLID principles
8. Documentation - comments, javadoc

Provide actionable feedback with specific examples and suggestions for improvement.
Format your review with clear sections and specific file/line references."""

    def _build_review_prompt(self, pr_context: Dict[str, Any]) -> str:
        """Build the user prompt for code review."""
        pr_diff = pr_context.get("pr_diff", "No diff available")
        pr_body = pr_context.get("body", "No description provided")

        # Limit diff size to avoid token limits
        if len(pr_diff) > 8000:
            pr_diff = pr_diff[:8000] + "\n... (truncated)"

        return f"""Please review the following pull request.

## PR Information
**Title:** {pr_context.get('title', 'N/A')}
**Branch:** {pr_context.get('head_branch', 'N/A')}

**Description:**
{pr_body}

## Code Changes
```diff
{pr_diff}
```

Please provide:
1. **Summary** - Brief overview of the changes
2. **Code Quality Issues** - Any clarity or maintainability concerns
3. **Error Handling** - Review of error handling and validation
4. **Test Coverage** - Assessment of test quality and coverage
5. **Security Concerns** - Any potential security issues
6. **Spring Boot Best Practices** - Adherence to patterns and conventions
7. **Performance Considerations** - Any performance implications
8. **Suggestions for Improvement** - Specific recommendations with examples
9. **Approval Status** - APPROVED, CHANGES_REQUESTED, or COMMENT

Format with clear sections and provide specific examples."""

    def parse_review(self, review_text: str) -> Dict[str, Any]:
        """
        Parse the review text to extract approval status and key points.

        Args:
            review_text: Raw review text

        Returns:
            Parsed review data
        """
        # Look for approval status
        approval_status = "COMMENT"
        if "APPROVED" in review_text.upper():
            approval_status = "APPROVED"
        elif "CHANGES_REQUESTED" in review_text.upper() or "CHANGES REQUESTED" in review_text.upper():
            approval_status = "CHANGES_REQUESTED"

        # Extract sections
        return {
            "full_review": review_text,
            "approval_status": approval_status,
        }

    def post_review(
        self, pr_number: int, review_text: str, approval_status: str
    ) -> Dict[str, Any]:
        """
        Post the code review to the PR.

        Args:
            pr_number: PR number
            review_text: Review content
            approval_status: Approval status (APPROVED, CHANGES_REQUESTED, COMMENT)

        Returns:
            Review response
        """
        print_step("Posting review to PR...")

        try:
            # Create a review comment
            review_response = self.github.create_pr_review(
                pr_number=pr_number,
                body=review_text,
                event=approval_status,
            )

            print_success(
                f"Review posted with status: {approval_status}"
            )

            return review_response

        except Exception as e:
            print_error(f"Failed to post review: {str(e)}")
            # Don't raise, continue anyway

            # Try posting as a regular comment instead
            try:
                formatted_review = f"""## Automated Code Review

{review_text}

---
*Generated by Agentic Workflow Orchestrator - Code Review Agent*
"""
                self.github.add_issue_comment(pr_number, formatted_review)
                print_success("Review posted as comment")
                return {"comment_posted": True}
            except Exception as e2:
                print_error(f"Failed to post review as comment: {str(e2)}")
                raise

    def run(self, pr_number: int) -> Dict[str, Any]:
        """
        Run the code review agent end-to-end.

        Args:
            pr_number: PR number to review

        Returns:
            Review results
        """
        print_agent_header(
            "REVIEW AGENT", f"Reviewing pull request #{pr_number}"
        )

        try:
            # Get PR context
            pr_context = self.get_pr_context(pr_number)

            # Analyze code
            review_text = self.analyze_pr(pr_context)

            # Parse review
            review_data = self.parse_review(review_text)

            # Post review
            self.post_review(
                pr_number, review_text, review_data["approval_status"]
            )

            return {
                "pr_number": pr_number,
                "review_text": review_text,
                "approval_status": review_data["approval_status"],
                "posted": True,
            }

        except Exception as e:
            print_error(f"Code review failed: {str(e)}")
            raise
