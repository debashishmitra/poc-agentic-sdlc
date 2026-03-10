"""
Story Reader Agent - reads and parses GitHub issues.
Extracts story data including title, acceptance criteria, technical notes, etc.
"""

import re
from typing import Dict, Any, List

from utils.formatting import print_agent_header, print_step, print_success, print_error
from utils.github_client import GitHubClient


class StoryReaderAgent:
    """Agent for reading and parsing GitHub issues as user stories."""

    def __init__(self):
        """Initialize the Story Reader Agent."""
        self.github = GitHubClient()

    def parse_issue(self, issue_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Parse a GitHub issue into structured story data.

        Args:
            issue_data: Raw issue data from GitHub API

        Returns:
            Structured story data
        """
        issue_number = issue_data.get("number")
        title = issue_data.get("title", "")
        body = issue_data.get("body", "")
        labels = [label["name"] for label in issue_data.get("labels", [])]

        # Extract story components from the body
        story_data = {
            "number": issue_number,
            "title": title,
            "body": body,
            "labels": labels,
            "acceptance_criteria": self._extract_section(body, "acceptance criteria"),
            "technical_notes": self._extract_section(body, "technical notes"),
            "story_points": self._extract_story_points(body, labels),
            "priority": self._extract_priority(body, labels),
            "description": self._extract_section(body, "description"),
            "implementation_notes": self._extract_section(
                body, "implementation notes"
            ),
        }

        return story_data

    def _extract_section(self, body: str, section_name: str) -> str:
        """
        Extract a section from the issue body.

        Args:
            body: Issue body text
            section_name: Name of the section to extract

        Returns:
            Section content or empty string
        """
        # Look for section headers like "## Acceptance Criteria" or "### Acceptance Criteria"
        pattern = rf"#+\s*{section_name}\s*\n(.*?)(?=\n#+\s|\Z)"
        match = re.search(pattern, body, re.IGNORECASE | re.DOTALL)

        if match:
            content = match.group(1).strip()
            return content
        return ""

    def _extract_story_points(self, body: str, labels: List[str]) -> int:
        """
        Extract story points from body or labels.

        Args:
            body: Issue body
            labels: Issue labels

        Returns:
            Story points (default: 5)
        """
        # Look for patterns like "story-points: 8" or "points: 8"
        pattern = r"(?:story-?)?points:\s*(\d+)"
        match = re.search(pattern, body, re.IGNORECASE)

        if match:
            return int(match.group(1))

        # Check labels for story-points-N
        for label in labels:
            if label.startswith("story-points-"):
                try:
                    return int(label.split("-")[-1])
                except ValueError:
                    pass

        return 5  # Default

    def _extract_priority(self, body: str, labels: List[str]) -> str:
        """
        Extract priority from body or labels.

        Args:
            body: Issue body
            labels: Issue labels

        Returns:
            Priority level (high, medium, low)
        """
        # Check labels first
        for label in labels:
            if label.lower() in ["priority-high", "p0", "critical"]:
                return "high"
            elif label.lower() in ["priority-medium", "p1"]:
                return "medium"
            elif label.lower() in ["priority-low", "p2"]:
                return "low"

        # Look in body
        if "high priority" in body.lower() or "urgent" in body.lower():
            return "high"
        elif "low priority" in body.lower():
            return "low"

        return "medium"  # Default

    def read_story(self, issue_number: int) -> Dict[str, Any]:
        """
        Read and parse a GitHub issue as a story.

        Args:
            issue_number: GitHub issue number

        Returns:
            Structured story data
        """
        print_agent_header("STORY READER", f"Reading issue #{issue_number}")

        try:
            print_step("Fetching issue from GitHub...")
            issue_data = self.github.get_issue(issue_number)

            print_step("Parsing story data...")
            story_data = self.parse_issue(issue_data)

            print_success(
                f"Story parsed successfully: '{story_data['title']}' "
                f"({story_data['story_points']} points, {story_data['priority']} priority)"
            )

            # Print story details
            self._print_story_details(story_data)

            return story_data

        except Exception as e:
            print_error(f"Failed to read story: {str(e)}")
            raise

    def _print_story_details(self, story_data: Dict[str, Any]):
        """Print story details to console."""
        print("\n  Story Details:")
        print(f"    Title: {story_data['title']}")
        print(f"    Points: {story_data['story_points']}")
        print(f"    Priority: {story_data['priority']}")

        if story_data.get("description"):
            desc = story_data["description"][:100].replace("\n", " ")
            print(f"    Description: {desc}...")

        if story_data.get("acceptance_criteria"):
            criteria_count = len(
                [
                    line
                    for line in story_data["acceptance_criteria"].split("\n")
                    if line.strip().startswith(("-", "*", "•", "1.", "2.", "3."))
                ]
            )
            print(f"    Acceptance Criteria: {criteria_count} items")

        if story_data.get("technical_notes"):
            print(f"    Has Technical Notes: Yes")

        print()
