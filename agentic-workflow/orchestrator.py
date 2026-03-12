"""
Main orchestrator for the agentic SDLC workflow.
Coordinates all agents to run the complete pipeline from issue to deployment.
"""

from typing import Dict, Any, Optional

import time

from agents.design_agent import DesignAgent
from agents.implementation_agent import ImplementationAgent
from agents.review_agent import ReviewAgent
from agents.story_reader import StoryReaderAgent
from utils.formatting import (
    print_banner,
    print_error,
    print_summary,
    print_section,
)


class WorkflowOrchestrator:
    """Orchestrates the agentic SDLC workflow."""

    def __init__(self):
        """Initialize the orchestrator with all agents."""
        self.story_reader = StoryReaderAgent()
        self.design_agent = DesignAgent()
        self.implementation_agent = ImplementationAgent()
        self.review_agent = ReviewAgent()

        self.workflow_state = {
            "story_data": None,
            "design_result": None,
            "implementation_result": None,
            "review_result": None,
        }

    def run_story_phase(self, issue_number: int) -> Dict[str, Any]:
        """
        Phase 1: Read and parse the story.

        Args:
            issue_number: GitHub issue number

        Returns:
            Story data
        """
        print_section("PHASE 1: STORY READING")

        try:
            story_data = self.story_reader.read_story(issue_number)
            self.workflow_state["story_data"] = story_data
            return story_data
        except Exception as e:
            print_error(f"Story reading failed: {str(e)}")
            raise

    def run_design_phase(self, story_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Phase 2: Generate technical design document.

        Args:
            story_data: Parsed story data

        Returns:
            Design result
        """
        print_section("PHASE 2: TECHNICAL DESIGN")

        try:
            design_result = self.design_agent.run(story_data)
            self.workflow_state["design_result"] = design_result

            print_summary(
                "Design Phase Complete",
                {
                    "Branch": design_result.get("branch"),
                    "File": design_result.get("file"),
                    "PR Number": str(design_result.get("pr_number")),
                },
            )

            return design_result
        except Exception as e:
            print_error(f"Design phase failed: {str(e)}")
            raise

    def run_implementation_phase(
        self, story_data: Dict[str, Any], design_result: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Phase 3: Generate implementation code.

        Args:
            story_data: Parsed story data
            design_result: Design phase result

        Returns:
            Implementation result
        """
        print_section("PHASE 3: IMPLEMENTATION")

        try:
            design_content = design_result.get("design_content", "")

            impl_result = self.implementation_agent.run(story_data, design_content)
            self.workflow_state["implementation_result"] = impl_result

            print_summary(
                "Implementation Phase Complete",
                {
                    "Branch": impl_result.get("branch"),
                    "Files Generated": str(impl_result.get("count", 0)),
                    "PR Number": str(impl_result.get("pr_number")),
                },
            )

            return impl_result
        except Exception as e:
            print_error(f"Implementation phase failed: {str(e)}")
            raise

    def run_review_phase(self, pr_number: int) -> Dict[str, Any]:
        """
        Phase 4: Perform automated code review.

        Args:
            pr_number: PR number to review

        Returns:
            Review result
        """
        print_section("PHASE 4: CODE REVIEW")

        try:
            review_result = self.review_agent.run(pr_number)
            self.workflow_state["review_result"] = review_result

            print_summary(
                "Code Review Complete",
                {
                    "PR Number": str(review_result.get("pr_number")),
                    "Status": review_result.get("approval_status"),
                },
            )

            return review_result
        except Exception as e:
            print_error(f"Code review failed: {str(e)}")
            raise

    def run_full_pipeline(self, issue_number: int) -> Dict[str, Any]:
        """
        Run the complete SDLC pipeline.

        Args:
            issue_number: GitHub issue number

        Returns:
            Complete workflow results
        """
        print_banner("AGENTIC SDLC WORKFLOW ORCHESTRATOR")
        print_info(f"Starting full pipeline for issue #{issue_number}\n")

        start_time = time.time()

        try:
            # Phase 1: Story Reading
            story_data = self.run_story_phase(issue_number)

            # Phase 2: Design
            design_result = self.run_design_phase(story_data)

            # Phase 3: Implementation
            impl_result = self.run_implementation_phase(story_data, design_result)

            # Phase 4: Code Review
            review_result = self.run_review_phase(impl_result["pr_number"])

            # Summary
            elapsed_time = time.time() - start_time
            self._print_workflow_summary(elapsed_time)

            return {
                "success": True,
                "story": story_data,
                "design": design_result,
                "implementation": impl_result,
                "review": review_result,
                "elapsed_time": elapsed_time,
            }

        except Exception as e:
            elapsed_time = time.time() - start_time
            print_error(f"Pipeline failed after {elapsed_time:.2f}s: {str(e)}")
            return {
                "success": False,
                "error": str(e),
                "elapsed_time": elapsed_time,
            }

    def run_phase(
        self,
        phase: str,
        issue_number: int,
        pr_number: Optional[int] = None,
    ) -> Dict[str, Any]:
        """
        Run a specific phase only.

        Args:
            phase: Phase name (story, design, implement, review)
            issue_number: Issue number
            pr_number: PR number (for review phase)

        Returns:
            Phase results
        """
        print_banner("AGENTIC SDLC WORKFLOW - SINGLE PHASE")

        try:
            if phase == "story":
                return self.run_story_phase(issue_number)

            elif phase == "design":
                story_data = self.run_story_phase(issue_number)
                return self.run_design_phase(story_data)

            elif phase == "implement":
                story_data = self.run_story_phase(issue_number)
                # For this mode, generate a mock design
                mock_design = {
                    "design_content": "Design document from previous phase"
                }
                return self.run_implementation_phase(story_data, mock_design)

            elif phase == "review":
                if not pr_number:
                    raise ValueError("PR number required for review phase")
                return self.run_review_phase(pr_number)

            else:
                raise ValueError(
                    f"Unknown phase: {phase}. "
                    "Valid options: story, design, implement, review"
                )

        except Exception as e:
            print_error(f"Phase '{phase}' failed: {str(e)}")
            return {
                "success": False,
                "error": str(e),
            }

    def _print_workflow_summary(self, elapsed_time: float):
        """Print a summary of the completed workflow."""
        print_section("WORKFLOW COMPLETE")

        summary_items = {
            "Status": "SUCCESS",
            "Total Time": f"{elapsed_time:.2f} seconds",
            "Story": f"#{self.workflow_state['story_data'].get('number')}",
        }

        if self.workflow_state["design_result"]:
            summary_items["Design PR"] = f"#{self.workflow_state['design_result'].get('pr_number')}"

        if self.workflow_state["implementation_result"]:
            summary_items["Implementation PR"] = f"#{self.workflow_state['implementation_result'].get('pr_number')}"

        if self.workflow_state["review_result"]:
            summary_items["Review Status"] = self.workflow_state["review_result"].get("approval_status")

        print_summary("Workflow Summary", summary_items)


def print_info(message: str):
    """Print an info message."""
    from utils.formatting import print_info as _print_info
    _print_info(message)
