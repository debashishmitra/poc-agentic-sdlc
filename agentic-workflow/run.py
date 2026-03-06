#!/usr/bin/env python3
"""
CLI entry point for the agentic SDLC workflow orchestrator.

Usage:
    python run.py --issue 1                           # Run full pipeline
    python run.py --issue 1 --phase design            # Run design phase only
    python run.py --issue 1 --phase implement         # Run implementation phase
    python run.py --issue 1 --phase review --pr 2     # Run review on PR
"""

import sys
import argparse
from pathlib import Path

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from orchestrator import WorkflowOrchestrator
from config.settings import validate_config
from utils.formatting import print_error, print_banner


def main():
    """Main entry point for the CLI."""
    parser = argparse.ArgumentParser(
        description="Agentic SDLC Workflow Orchestrator",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run.py --issue 1                    # Run full pipeline for issue #1
  python run.py --issue 1 --phase design     # Run only design phase
  python run.py --issue 1 --phase implement  # Run only implementation phase
  python run.py --issue 1 --phase review --pr 2  # Review PR #2
        """,
    )

    parser.add_argument(
        "--issue",
        type=int,
        required=True,
        help="GitHub issue number",
    )

    parser.add_argument(
        "--phase",
        choices=["story", "design", "implement", "review"],
        default=None,
        help="Specific phase to run (default: run all phases)",
    )

    parser.add_argument(
        "--pr",
        type=int,
        default=None,
        help="PR number (required for review phase)",
    )

    parser.add_argument(
        "--verbose",
        "-v",
        action="store_true",
        help="Enable verbose output",
    )

    args = parser.parse_args()

    try:
        # Validate configuration
        validate_config()

        # Create orchestrator
        orchestrator = WorkflowOrchestrator()

        # Run appropriate pipeline
        if args.phase:
            result = orchestrator.run_phase(
                phase=args.phase,
                issue_number=args.issue,
                pr_number=args.pr,
            )
        else:
            result = orchestrator.run_full_pipeline(args.issue)

        # Print results
        if result.get("success", False) or "design" in result:
            sys.exit(0)
        else:
            sys.exit(1)

    except ValueError as e:
        print_error(f"Configuration error: {str(e)}")
        sys.exit(1)
    except KeyboardInterrupt:
        print_error("Workflow interrupted by user")
        sys.exit(130)
    except Exception as e:
        print_error(f"Unexpected error: {str(e)}")
        if args.verbose:
            import traceback
            traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
