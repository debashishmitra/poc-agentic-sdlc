"""
Console formatting utilities for agent output.
Provides colorized and formatted output for status messages.
"""

import io
import sys

# Force UTF-8 output on Windows to support Unicode characters
if sys.stdout.encoding != "utf-8":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
if sys.stderr.encoding != "utf-8":
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8", errors="replace")

# ANSI color codes
class Colors:
    """ANSI color codes."""
    RESET = "\033[0m"
    BOLD = "\033[1m"
    DIM = "\033[2m"

    # Foreground colors
    BLACK = "\033[30m"
    RED = "\033[31m"
    GREEN = "\033[32m"
    YELLOW = "\033[33m"
    BLUE = "\033[34m"
    MAGENTA = "\033[35m"
    CYAN = "\033[36m"
    WHITE = "\033[37m"

    # Background colors
    BG_BLACK = "\033[40m"
    BG_RED = "\033[41m"
    BG_GREEN = "\033[42m"
    BG_YELLOW = "\033[43m"
    BG_BLUE = "\033[44m"
    BG_MAGENTA = "\033[45m"
    BG_CYAN = "\033[46m"
    BG_WHITE = "\033[47m"


def print_agent_header(agent_name: str, message: str = ""):
    """
    Print a formatted agent header.

    Args:
        agent_name: Name of the agent
        message: Additional message
    """
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'='*70}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}🤖 {agent_name}{Colors.RESET}")
    if message:
        print(f"{Colors.CYAN}{message}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'='*70}{Colors.RESET}\n")


def print_step(message: str):
    """
    Print a step message.

    Args:
        message: Step description
    """
    print(f"{Colors.BLUE}⚙️  {message}{Colors.RESET}")


def print_success(message: str):
    """
    Print a success message.

    Args:
        message: Success description
    """
    print(f"{Colors.GREEN}✓ {message}{Colors.RESET}")


def print_error(message: str):
    """
    Print an error message.

    Args:
        message: Error description
    """
    print(f"{Colors.RED}✗ {message}{Colors.RESET}")


def print_warning(message: str):
    """
    Print a warning message.

    Args:
        message: Warning description
    """
    print(f"{Colors.YELLOW}⚠️  {message}{Colors.RESET}")


def print_info(message: str):
    """
    Print an info message.

    Args:
        message: Info description
    """
    print(f"{Colors.MAGENTA}ℹ️  {message}{Colors.RESET}")


def print_section(title: str):
    """
    Print a section header.

    Args:
        title: Section title
    """
    print(f"\n{Colors.BOLD}{Colors.YELLOW}━━━ {title} ━━━{Colors.RESET}")


def print_result(key: str, value: str):
    """
    Print a key-value result pair.

    Args:
        key: Result key
        value: Result value
    """
    print(f"{Colors.DIM}{key}:{Colors.RESET} {value}")


def print_summary(title: str, items: dict):
    """
    Print a formatted summary.

    Args:
        title: Summary title
        items: Dictionary of items to display
    """
    print(f"\n{Colors.BOLD}{Colors.GREEN}📋 {title}{Colors.RESET}")
    for key, value in items.items():
        print(f"  {Colors.DIM}{key}:{Colors.RESET} {value}")
    print()


def print_banner(message: str, width: int = 70):
    """
    Print a centered banner message.

    Args:
        message: Message to display
        width: Banner width
    """
    print(f"{Colors.BOLD}{Colors.MAGENTA}{'*'*width}{Colors.RESET}")
    centered = message.center(width)
    print(f"{Colors.BOLD}{Colors.MAGENTA}{centered}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.MAGENTA}{'*'*width}{Colors.RESET}\n")
