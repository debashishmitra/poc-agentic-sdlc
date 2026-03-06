"""
GitHub API client for interacting with the repository.
Handles issue reading, branch creation, file operations, and PR management.
"""

import requests
import json
from typing import Dict, Any, Optional
from datetime import datetime
from config.settings import GITHUB_TOKEN, GITHUB_REPO, GITHUB_API_BASE, API_TIMEOUT


class GitHubClient:
    """Client for interacting with GitHub API."""

    def __init__(self):
        """Initialize the GitHub client with authentication headers."""
        self.base_url = GITHUB_API_BASE
        self.repo = GITHUB_REPO
        self.headers = {
            "Authorization": f"token {GITHUB_TOKEN}",
            "Accept": "application/vnd.github.v3+json",
            "Content-Type": "application/json",
        }
        self.timeout = API_TIMEOUT

    def _make_request(
        self,
        method: str,
        endpoint: str,
        data: Optional[Dict[str, Any]] = None,
        params: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """
        Make an HTTP request to the GitHub API.

        Args:
            method: HTTP method (GET, POST, PUT, PATCH, DELETE)
            endpoint: API endpoint path (without base URL)
            data: Request body data
            params: Query parameters

        Returns:
            Response JSON data

        Raises:
            Exception: If the request fails
        """
        url = f"{self.base_url}{endpoint}"

        try:
            response = requests.request(
                method=method,
                url=url,
                headers=self.headers,
                json=data,
                params=params,
                timeout=self.timeout,
            )
            response.raise_for_status()
            return response.json() if response.text else {}
        except requests.exceptions.RequestException as e:
            raise Exception(f"GitHub API request failed: {str(e)}")

    def get_issue(self, issue_number: int) -> Dict[str, Any]:
        """
        Fetch a GitHub issue.

        Args:
            issue_number: Issue number to fetch

        Returns:
            Issue data including title, body, labels, etc.
        """
        endpoint = f"/repos/{self.repo}/issues/{issue_number}"
        return self._make_request("GET", endpoint)

    def create_branch(self, branch_name: str, base: str = "main") -> Dict[str, Any]:
        """
        Create a new branch.

        Args:
            branch_name: Name of the new branch
            base: Base branch to create from (default: main)

        Returns:
            Branch creation response
        """
        # First, get the SHA of the base branch
        endpoint = f"/repos/{self.repo}/git/refs/heads/{base}"
        base_ref = self._make_request("GET", endpoint)
        base_sha = base_ref["object"]["sha"]

        # Create the new branch
        endpoint = f"/repos/{self.repo}/git/refs"
        data = {
            "ref": f"refs/heads/{branch_name}",
            "sha": base_sha,
        }
        return self._make_request("POST", endpoint, data)

    def get_file_content(
        self, path: str, branch: str = "main"
    ) -> Optional[Dict[str, Any]]:
        """
        Read a file from the repository.

        Args:
            path: File path in the repository
            branch: Branch name (default: main)

        Returns:
            File content and metadata, or None if file not found
        """
        endpoint = f"/repos/{self.repo}/contents/{path}"
        params = {"ref": branch}

        try:
            return self._make_request("GET", endpoint, params=params)
        except Exception:
            return None

    def create_or_update_file(
        self,
        path: str,
        content: str,
        message: str,
        branch: str = "main",
    ) -> Dict[str, Any]:
        """
        Create or update a file in the repository.

        Args:
            path: File path in the repository
            content: File content
            message: Commit message
            branch: Branch name (default: main)

        Returns:
            Commit response
        """
        import base64

        endpoint = f"/repos/{self.repo}/contents/{path}"

        # Get existing file SHA if it exists
        existing = self.get_file_content(path, branch)
        sha = existing.get("sha") if existing else None

        data = {
            "message": message,
            "content": base64.b64encode(content.encode()).decode(),
            "branch": branch,
        }

        if sha:
            data["sha"] = sha

        return self._make_request("PUT", endpoint, data)

    def create_pull_request(
        self,
        title: str,
        body: str,
        head_branch: str,
        base: str = "main",
    ) -> Dict[str, Any]:
        """
        Create a pull request.

        Args:
            title: PR title
            body: PR description
            head_branch: Source branch
            base: Target branch (default: main)

        Returns:
            PR data
        """
        endpoint = f"/repos/{self.repo}/pulls"
        data = {
            "title": title,
            "body": body,
            "head": head_branch,
            "base": base,
        }
        return self._make_request("POST", endpoint, data)

    def add_issue_comment(self, issue_number: int, body: str) -> Dict[str, Any]:
        """
        Add a comment to a GitHub issue.

        Args:
            issue_number: Issue number
            body: Comment text

        Returns:
            Comment data
        """
        endpoint = f"/repos/{self.repo}/issues/{issue_number}/comments"
        data = {"body": body}
        return self._make_request("POST", endpoint, data)

    def update_issue_labels(
        self, issue_number: int, labels: list
    ) -> Dict[str, Any]:
        """
        Update labels on a GitHub issue.

        Args:
            issue_number: Issue number
            labels: List of label names

        Returns:
            Updated issue data
        """
        endpoint = f"/repos/{self.repo}/issues/{issue_number}/labels"
        data = labels
        return self._make_request("POST", endpoint, data)

    def get_pr(self, pr_number: int) -> Dict[str, Any]:
        """
        Fetch a pull request.

        Args:
            pr_number: PR number

        Returns:
            PR data
        """
        endpoint = f"/repos/{self.repo}/pulls/{pr_number}"
        return self._make_request("GET", endpoint)

    def get_pr_diff(self, pr_number: int) -> str:
        """
        Get the diff for a pull request.

        Args:
            pr_number: PR number

        Returns:
            Diff content
        """
        endpoint = f"/repos/{self.repo}/pulls/{pr_number}"
        headers = self.headers.copy()
        headers["Accept"] = "application/vnd.github.v3.diff"

        try:
            response = requests.get(
                f"{self.base_url}{endpoint}",
                headers=headers,
                timeout=self.timeout,
            )
            response.raise_for_status()
            return response.text
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to get PR diff: {str(e)}")

    def create_pr_review(
        self, pr_number: int, body: str, event: str = "COMMENT"
    ) -> Dict[str, Any]:
        """
        Create a review on a pull request.

        Args:
            pr_number: PR number
            body: Review comment
            event: Review event (APPROVE, REQUEST_CHANGES, COMMENT)

        Returns:
            Review data
        """
        endpoint = f"/repos/{self.repo}/pulls/{pr_number}/reviews"
        data = {
            "body": body,
            "event": event,
        }
        return self._make_request("POST", endpoint, data)

    def create_pr_review_comment(
        self, pr_number: int, commit_id: str, path: str, line: int, body: str
    ) -> Dict[str, Any]:
        """
        Create a comment on a specific line in a PR.

        Args:
            pr_number: PR number
            commit_id: Commit SHA
            path: File path
            line: Line number
            body: Comment text

        Returns:
            Comment data
        """
        endpoint = f"/repos/{self.repo}/pulls/{pr_number}/comments"
        data = {
            "commit_id": commit_id,
            "path": path,
            "line": line,
            "body": body,
        }
        return self._make_request("POST", endpoint, data)

    def get_issue_comments(self, issue_number: int) -> list:
        """
        Get all comments on an issue.

        Args:
            issue_number: Issue number

        Returns:
            List of comments
        """
        endpoint = f"/repos/{self.repo}/issues/{issue_number}/comments"
        return self._make_request("GET", endpoint)

    def get_commits(self, branch: str = "main", per_page: int = 10) -> list:
        """
        Get recent commits from a branch.

        Args:
            branch: Branch name
            per_page: Number of commits to fetch

        Returns:
            List of commits
        """
        endpoint = f"/repos/{self.repo}/commits"
        params = {"sha": branch, "per_page": per_page}
        return self._make_request("GET", endpoint, params=params)
