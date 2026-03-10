"""
Configuration settings for the agentic workflow orchestrator.
Loads settings from environment variables and provides defaults.
"""

import os
from pathlib import Path


def _load_env_file(env_path: Path):
    """Load environment variables from a .env file."""
    if not env_path.exists():
        return
    with open(env_path) as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, value = line.partition("=")
            key = key.strip()
            value = value.strip().strip('"').strip("'")
            if key and key not in os.environ:
                os.environ[key] = value


# Load .env file from the agentic-workflow directory
_load_env_file(Path(__file__).parent.parent / ".env")

# GitHub configuration
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN", "")
GITHUB_REPO = "debashishmitra/poc-agentic-sdlc"
GITHUB_API_BASE = "https://api.github.com"

# Anthropic API configuration
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
# MODEL = "claude-sonnet-4-20250514"
MODEL = "claude-opus-4-6"

# Repository local path
REPO_LOCAL_PATH = os.getenv("REPO_LOCAL_PATH", str(Path(__file__).parent.parent.parent))

# Validate required environment variables
def validate_config():
    """Validate that required environment variables are set."""
    errors = []

    if not GITHUB_TOKEN:
        errors.append("GITHUB_TOKEN environment variable is not set")

    if not ANTHROPIC_API_KEY:
        errors.append("ANTHROPIC_API_KEY environment variable is not set")

    if errors:
        raise ValueError("Configuration errors:\n" + "\n".join(f"  - {e}" for e in errors))


# API request timeout (seconds) - design/implementation need longer timeouts
API_TIMEOUT = 120

# Maximum tokens for Claude API responses
MAX_TOKENS = 8192

# Claude models configuration
CLAUDE_MODELS = {
    "sonnet": "claude-sonnet-4-20250514",
    "opus": "claude-opus-4-6",
}

# Default model for different tasks
DESIGN_MODEL = MODEL
IMPLEMENTATION_MODEL = MODEL
REVIEW_MODEL = MODEL
