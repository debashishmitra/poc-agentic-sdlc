"""
Claude API client for interacting with Anthropic's Claude models.
Handles API calls with proper error handling and fallback strategies.
"""

import json
from typing import Dict

import requests
from config.settings import ANTHROPIC_API_KEY, MODEL, MAX_TOKENS, API_TIMEOUT


class ClaudeClient:
    """Client for interacting with Anthropic's Claude API."""

    def __init__(self, model: str = MODEL):
        """
        Initialize the Claude client.

        Args:
            model: Model name to use (default: from settings)
        """
        self.model = model
        self.api_key = ANTHROPIC_API_KEY
        self.base_url = "https://api.anthropic.com/v1"
        self.timeout = API_TIMEOUT

    def generate(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = MAX_TOKENS,
    ) -> str:
        """
        Generate a response from Claude.

        Args:
            system_prompt: System prompt for Claude
            user_prompt: User prompt/message
            max_tokens: Maximum tokens in response

        Returns:
            Generated text response

        Raises:
            Exception: If API call fails
        """
        endpoint = f"{self.base_url}/messages"

        headers = {
            "anthropic-version": "2023-06-01",
            "content-type": "application/json",
            "x-api-key": self.api_key,
        }

        data = {
            "model": self.model,
            "max_tokens": max_tokens,
            "system": system_prompt,
            "messages": [
                {
                    "role": "user",
                    "content": user_prompt,
                }
            ],
        }

        try:
            response = requests.post(
                endpoint,
                headers=headers,
                json=data,
                timeout=self.timeout,
            )
            response.raise_for_status()

            result = response.json()

            # Extract the text from the response
            if "content" in result and len(result["content"]) > 0:
                return result["content"][0]["text"]
            else:
                raise Exception("No content in Claude API response")

        except requests.exceptions.RequestException as e:
            raise Exception(f"Claude API request failed: {str(e)}")
        except json.JSONDecodeError as e:
            raise Exception(f"Failed to parse Claude API response: {str(e)}")

    def generate_with_context(
        self,
        system_prompt: str,
        user_prompt: str,
        context_files: Dict[str, str],
        max_tokens: int = MAX_TOKENS,
    ) -> str:
        """
        Generate a response from Claude with file context.

        Args:
            system_prompt: System prompt for Claude
            user_prompt: User prompt/message
            context_files: Dict of filename -> file_content for context
            max_tokens: Maximum tokens in response

        Returns:
            Generated text response
        """
        # Build context string from files
        context_str = "\n".join(
            f"=== File: {name} ===\n{content}\n"
            for name, content in context_files.items()
        )

        # Append context to user prompt
        full_prompt = f"{user_prompt}\n\n## Relevant Code Context:\n{context_str}"

        return self.generate(system_prompt, full_prompt, max_tokens)

    def stream_generate(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = MAX_TOKENS,
    ):
        """
        Generate a response from Claude with streaming (experimental).

        Args:
            system_prompt: System prompt for Claude
            user_prompt: User prompt/message
            max_tokens: Maximum tokens in response

        Yields:
            Streamed text chunks
        """
        endpoint = f"{self.base_url}/messages"

        headers = {
            "anthropic-version": "2023-06-01",
            "content-type": "application/json",
            "x-api-key": self.api_key,
        }

        data = {
            "model": self.model,
            "max_tokens": max_tokens,
            "system": system_prompt,
            "messages": [
                {
                    "role": "user",
                    "content": user_prompt,
                }
            ],
            "stream": True,
        }

        try:
            response = requests.post(
                endpoint,
                headers=headers,
                json=data,
                timeout=self.timeout,
                stream=True,
            )
            response.raise_for_status()

            for line in response.iter_lines():
                if line:
                    line_str = line.decode("utf-8")
                    if line_str.startswith("data: "):
                        data_str = line_str[6:]
                        if data_str == "[DONE]":
                            break
                        try:
                            event = json.loads(data_str)
                            if "content_block_delta" in event:
                                delta = event["content_block_delta"].get("delta", {})
                                if "text" in delta:
                                    yield delta["text"]
                        except json.JSONDecodeError:
                            continue

        except requests.exceptions.RequestException as e:
            raise Exception(f"Claude API streaming failed: {str(e)}")

    def count_tokens(self, text: str) -> int:
        """
        Estimate token count (rough approximation).

        Args:
            text: Text to count tokens for

        Returns:
            Estimated token count
        """
        # Rough approximation: ~4 characters per token
        return len(text) // 4
