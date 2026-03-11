# Agentic Workflow Orchestrator - Project Overview

## Table of Contents

- [Project Summary](#project-summary)
- [Location](#location)
- [Core Concept](#core-concept)
- [Four-Phase Pipeline](#four-phase-pipeline)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Key Components](#key-components)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Data Flow](#data-flow)
- [Workflow State Management](#workflow-state-management)
- [Error Handling](#error-handling)
- [Console Output](#console-output)
- [Claude API Integration](#claude-api-integration)
- [Performance Characteristics](#performance-characteristics)
- [Extensibility Points](#extensibility-points)
- [Future Enhancements](#future-enhancements)
- [Security Considerations](#security-considerations)
- [Testing & Quality](#testing--quality)
- [Key Files and Their Roles](#key-files-and-their-roles)
- [Deployment & Distribution](#deployment--distribution)
- [Repository Integration](#repository-integration)
- [Summary](#summary)

## Project Summary

The **Agentic Workflow Orchestrator** is the core component of the poc-agentic-sdlc proof-of-concept. It demonstrates how AI (Claude) can be orchestrated to automate the entire software development lifecycle (SDLC), from reading requirements to shipping code with reviews.

## Location

```
agentic-workflow/
```

## Core Concept

Instead of engineers manually handling each phase of SDLC:
1. Story analysis and breakdown
2. Design document creation
3. Code implementation
4. Code review and quality checks

The **orchestrator** coordinates specialized AI agents to perform each step, maintaining context and creating artifacts (design docs, code, reviews) at each stage.

## Four-Phase Pipeline

### Phase 1: Story Reader
- **Input**: GitHub Issue
- **Agent**: StoryReaderAgent
- **Output**: Structured story data
- **Actions**:
  - Reads issue from GitHub API
  - Extracts title, description, acceptance criteria
  - Parses technical notes, story points, priority
  - Returns structured JSON-like dictionary

### Phase 2: Design
- **Input**: Story data
- **Agent**: DesignAgent
- **Output**: Technical design document + PR
- **Actions**:
  - Reads codebase context from GitHub
  - Prompts Claude to generate design document
  - Creates `design/STORY-{N}` branch
  - Commits design to `docs/designs/Design-STORY-{N}.md`
  - Creates pull request for design review
  - Adds linking comment to original issue

**Design Document Contents**:
- Overview and objectives
- REST API specifications
- Data model changes and schema
- Architecture diagrams (Mermaid)
- Service layer design
- Testing strategy
- Error handling approach
- Performance considerations

### Phase 3: Implementation
- **Input**: Story data + Design document
- **Agent**: ImplementationAgent
- **Output**: Implementation code + PR
- **Actions**:
  - Reads code patterns from existing repo
  - Prompts Claude to generate code for multiple components
  - Creates `feature/STORY-{N}` branch
  - Commits 6+ Java/Spring Boot files:
    - Entity/Model class (JPA)
    - Repository interface (Spring Data)
    - Service class (business logic)
    - Controller class (REST endpoints)
    - Unit tests (JUnit 5 + Mockito)
    - Integration tests (Spring Boot Test)
  - Creates implementation pull request

**Generated Code Quality**:
- Spring Boot best practices
- Proper annotation usage
- Dependency injection
- Error handling and validation
- Logging statements
- Test coverage

### Phase 4: Code Review
- **Input**: Implementation PR
- **Agent**: ReviewAgent
- **Output**: Review comments + approval status
- **Actions**:
  - Fetches PR diff from GitHub
  - Prompts Claude to perform code review
  - Posts review on PR with recommendations
  - Sets approval status (APPROVED/CHANGES_REQUESTED/COMMENT)

**Review Criteria**:
- Code quality and maintainability
- Error handling completeness
- Test coverage assessment
- Security vulnerability detection
- Spring Boot pattern compliance
- Performance implications
- Java/code standards

## Technology Stack

### Core Technologies
- **Language**: Python 3.8+
- **APIs**: GitHub REST API, Anthropic Claude API
- **HTTP Client**: requests library
- **Code Format**: Pure Python with no external frameworks

### Claude AI
- **Model**: claude-opus-4-6 (can be configured)
- **API Endpoint**: https://api.anthropic.com/v1/messages
- **Max Tokens**: 4096 per request (configurable)

### GitHub Integration
- **API**: GitHub REST API v3
- **Authentication**: Personal Access Token
- **Operations**: Issues, branches, pull requests, comments

## Project Structure

```
agentic-workflow/
├── config/
│   ├── __init__.py
│   └── settings.py                    # Configuration, env vars
├── agents/
│   ├── __init__.py
│   ├── story_reader.py               # Phase 1: Story reading
│   ├── design_agent.py               # Phase 2: Design generation
│   ├── implementation_agent.py       # Phase 3: Code generation
│   └── review_agent.py               # Phase 4: Code review
├── utils/
│   ├── __init__.py
│   ├── github_client.py              # GitHub API wrapper
│   ├── claude_client.py              # Claude API wrapper
│   └── formatting.py                 # Console formatting
├── orchestrator.py                    # Main workflow coordinator
├── run.py                            # CLI entry point
├── requirements.txt                  # Dependencies
├── README.md                         # Full documentation
├── QUICKSTART.md                     # Quick start guide
└── __init__.py                       # Package init
```

## Key Components

### 1. StoryReaderAgent (agents/story_reader.py)

Reads GitHub issues and extracts structured data.

**Key Methods**:
```python
def read_story(self, issue_number: int) -> Dict[str, Any]
def parse_issue(self, issue_data: Dict[str, Any]) -> Dict[str, Any]
def _extract_section(self, body: str, section_name: str) -> str
def _extract_story_points(self, body: str, labels: List[str]) -> int
def _extract_priority(self, body: str, labels: List[str]) -> str
```

**Output Format**:
```python
{
    "number": 1,
    "title": "Add User Authentication",
    "description": "...",
    "acceptance_criteria": "...",
    "technical_notes": "...",
    "story_points": 8,
    "priority": "high",
    "labels": ["feature", "backend"]
}
```

### 2. DesignAgent (agents/design_agent.py)

Generates technical design documents using Claude.

**Key Methods**:
```python
def generate_design(self, story_data: Dict[str, Any]) -> str
def read_repo_context(self) -> Dict[str, str]
def commit_design_doc(self, design_content: str, story_number: int)
def create_design_pr(self, story_number: int, design_content: str)
def run(self, story_data: Dict[str, Any]) -> Dict[str, Any]
```

**Process**:
1. Reads existing code patterns from repository
2. Prepares comprehensive system prompt
3. Calls Claude with story and context
4. Creates branch and commits document
5. Creates pull request for review
6. Links to original issue

### 3. ImplementationAgent (agents/implementation_agent.py)

Generates implementation code based on design.

**Key Methods**:
```python
def generate_implementation(self, story_data, design_content) -> Dict[str, str]
def read_repo_patterns(self) -> Dict[str, str]
def extract_classes_from_code(self, implementation_code) -> Dict[str, tuple]
def commit_implementation(self, implementation_code, story_number)
def create_implementation_pr(self, story_number, implementation_files)
def run(self, story_data, design_content) -> Dict[str, Any]
```

**Code Components Generated**:
- Entity class (JPA @Entity)
- Repository interface (Spring Data CrudRepository)
- Service class (@Service, @Transactional)
- Controller class (@RestController, @RequestMapping)
- Unit tests (JUnit 5, Mockito)
- Integration tests (Spring Boot Test)

### 4. ReviewAgent (agents/review_agent.py)

Performs automated code review.

**Key Methods**:
```python
def get_pr_context(self, pr_number: int) -> Dict[str, Any]
def analyze_pr(self, pr_context: Dict[str, Any]) -> str
def parse_review(self, review_text: str) -> Dict[str, Any]
def post_review(self, pr_number, review_text, approval_status)
def run(self, pr_number: int) -> Dict[str, Any]
```

**Review Process**:
1. Fetches PR and diff from GitHub
2. Prepares code review system prompt
3. Calls Claude to analyze code
4. Parses approval status from review
5. Posts review on GitHub PR

### 5. WorkflowOrchestrator (orchestrator.py)

Main coordinator for the entire pipeline.

**Key Methods**:
```python
def run_story_phase(self, issue_number: int) -> Dict[str, Any]
def run_design_phase(self, story_data: Dict[str, Any]) -> Dict[str, Any]
def run_implementation_phase(self, story_data, design_result) -> Dict[str, Any]
def run_review_phase(self, pr_number: int) -> Dict[str, Any]
def run_full_pipeline(self, issue_number: int) -> Dict[str, Any]
def run_phase(self, phase: str, issue_number: int, pr_number) -> Dict[str, Any]
```

### 6. GitHubClient (utils/github_client.py)

Wrapper around GitHub REST API.

**Key Methods**:
```python
def get_issue(self, issue_number: int) -> Dict[str, Any]
def create_branch(self, branch_name: str, base: str = "main")
def get_file_content(self, path: str, branch: str = "main")
def create_or_update_file(self, path, content, message, branch)
def create_pull_request(self, title, body, head_branch, base)
def add_issue_comment(self, issue_number, body)
def get_pr(self, pr_number: int) -> Dict[str, Any]
def get_pr_diff(self, pr_number: int) -> str
def create_pr_review(self, pr_number, body, event)
```

### 7. ClaudeClient (utils/claude_client.py)

Wrapper around Anthropic Claude API.

**Key Methods**:
```python
def generate(self, system_prompt, user_prompt, max_tokens) -> str
def generate_with_context(self, system_prompt, user_prompt, context_files)
def stream_generate(self, system_prompt, user_prompt, max_tokens)
def count_tokens(self, text: str) -> int
```

**API Details**:
- Endpoint: `https://api.anthropic.com/v1/messages`
- Version: 2023-06-01
- Authentication: x-api-key header

## Configuration

### Environment Variables
```bash
GITHUB_TOKEN              # GitHub personal access token (required)
ANTHROPIC_API_KEY        # Anthropic API key (required)
REPO_LOCAL_PATH          # Path to local repository (optional)
```

### settings.py
```python
GITHUB_REPO = "debashishmitra/poc-agentic-sdlc"
GITHUB_API_BASE = "https://api.anthropic.com/v1"
MODEL = "claude-opus-4-6"
CLAUDE_MODELS = {"sonnet": "claude-sonnet-4-6", "opus": "claude-opus-4-6"}
API_TIMEOUT = 30
MAX_TOKENS = 4096
```

## Usage Examples

### Full Pipeline
```bash
python run.py --issue 1
```

### Individual Phases
```bash
python run.py --issue 1 --phase story
python run.py --issue 1 --phase design
python run.py --issue 1 --phase implement
python run.py --issue 1 --phase review --pr 2
```

### Programmatic Usage
```python
from orchestrator import WorkflowOrchestrator

orchestrator = WorkflowOrchestrator()

# Run full pipeline
result = orchestrator.run_full_pipeline(issue_number=1)

# Or run individual phases
story = orchestrator.run_story_phase(1)
design = orchestrator.run_design_phase(story)
impl = orchestrator.run_implementation_phase(story, design)
review = orchestrator.run_review_phase(impl["pr_number"])
```

## Data Flow

```
GitHub Issue #1
    ↓ (StoryReaderAgent.read_story)
Story Dictionary
    {
        "number": 1,
        "title": "...",
        "description": "...",
        "acceptance_criteria": "...",
        "technical_notes": "...",
        "story_points": 8,
        "priority": "high"
    }
    ↓ (DesignAgent.generate_design)
Design Document (MD)
    [API Specs] [Data Model] [Architecture] [Service Design] [Testing]
    ↓ (Create PR)
Design PR #X
    ↓ (ImplementationAgent.generate_implementation)
Implementation Code (6+ Java files)
    [Entity] [Repository] [Service] [Controller] [Unit Tests] [Integration Tests]
    ↓ (Create PR)
Implementation PR #Y
    ↓ (ReviewAgent.analyze_pr)
Code Review (MD)
    [Summary] [Issues] [Suggestions] [Status: APPROVED/CHANGES_REQUESTED]
    ↓ (Post Review)
Review on PR #Y
```

## Workflow State Management

The orchestrator maintains state throughout the pipeline:

```python
workflow_state = {
    "story_data": Dict,              # From Phase 1
    "design_result": Dict,           # From Phase 2
    "implementation_result": Dict,   # From Phase 3
    "review_result": Dict            # From Phase 4
}
```

Each phase reads from previous phases via method parameters, ensuring:
- Context flows forward through the pipeline
- Each phase can be run independently with provided context
- No shared state issues between concurrent runs

## Error Handling

The system implements defensive error handling:

1. **Configuration Validation**: Checks env vars on startup
2. **API Error Handling**: Catches and logs API failures
3. **Graceful Degradation**: Falls back to alternatives (e.g., comment instead of review)
4. **Phase Independence**: Later phases can run even if earlier ones fail
5. **Detailed Error Messages**: Provides context and debugging info

## Console Output

Rich, formatted console output with:
- **Colors**: ANSI color codes for visual feedback
- **Progress**: Step indicators with emojis
- **Status**: Success/error/warning messages
- **Summaries**: Formatted result tables
- **Sections**: Clear phase boundaries

## Claude API Integration

### Design Generation
- **Model**: claude-opus-4-6
- **Max Tokens**: 6000 (allows longer design docs)
- **System Prompt**: Architect role, design expert
- **User Prompt**: Story + code context + design requirements

### Implementation Generation
- **Model**: claude-opus-4-6
- **Max Tokens**: 3000 per component (6 components total)
- **System Prompt**: Java/Spring Boot expert developer
- **User Prompt**: Story + design doc + existing patterns

### Code Review
- **Model**: claude-opus-4-6
- **Max Tokens**: 4000 (comprehensive review)
- **System Prompt**: Code review expert
- **User Prompt**: PR title + body + full diff

## Performance Characteristics

- **Story Reading**: 1-2 seconds
- **Design Generation**: 30-60 seconds (depends on Claude response time)
- **Implementation**: 60-120 seconds (multiple Claude calls)
- **Code Review**: 30-60 seconds
- **Total Pipeline**: 2-5 minutes for complete flow

## Extensibility Points

### Adding New Agents
1. Create class inheriting from agent pattern
2. Implement `run()` method
3. Add to orchestrator workflow
4. Update CLI

### Custom Generation Prompts
1. Edit system prompts in agent classes
2. Adjust max_tokens for longer responses
3. Modify user prompts for different styles

### Additional Languages
1. Modify implementation agent prompts
2. Update code extraction regex
3. Adjust file paths for new languages

### Integration Points
1. GitHub API via GitHubClient
2. Claude API via ClaudeClient
3. Custom formatters in utils/formatting.py

## Future Enhancements

1. **Approval Gating**: Wait for design PR approval before implementing
2. **Deployment Automation**: Auto-merge and deploy after review
3. **Multi-Language**: Python, Go, JavaScript code generation
4. **Database Migrations**: Generate schema migration scripts
5. **Documentation**: Auto-generate API docs, README, etc.
6. **Metrics**: Track quality metrics, coverage, performance
7. **Monitoring**: Integration with monitoring/observability tools
8. **GitHub Actions**: Automatic PR status checks

## Security Considerations

1. **Token Storage**: Uses environment variables, never hardcoded
2. **API Key Security**: Requires explicit env var setup
3. **Code Review**: Claude reviews all generated code
4. **No Secrets in Commits**: Design avoids embedding secrets
5. **GitHub Token Scoping**: Recommend minimal token permissions

## Testing & Quality

The implementation includes:
- Comprehensive error handling
- Input validation
- API response validation
- Graceful failure modes
- Detailed logging and console output

For manual testing:
1. Create test GitHub issue with clear requirements
2. Run design phase, verify design doc quality
3. Run implementation phase, verify code compiles
4. Run review phase, verify feedback quality
5. Check generated PRs and comments on GitHub

## Key Files and Their Roles

| File | LOC | Purpose |
|------|-----|---------|
| run.py | 100 | CLI entry point, argument parsing |
| orchestrator.py | 300 | Main workflow coordinator |
| story_reader.py | 200 | Story parsing and extraction |
| design_agent.py | 350 | Design generation and PR creation |
| implementation_agent.py | 450 | Code generation and commits |
| review_agent.py | 250 | Code review and feedback |
| github_client.py | 350 | GitHub API wrapper |
| claude_client.py | 250 | Claude API wrapper |
| formatting.py | 150 | Console output formatting |
| settings.py | 100 | Configuration management |
| requirements.txt | 3 | Python dependencies |
| README.md | 600 | Full documentation |
| QUICKSTART.md | 400 | Quick start guide |

## Deployment & Distribution

The orchestrator can be:
1. **Installed Locally**: Clone repo, install dependencies, run
2. **Docker Image**: Build container for consistent environment
3. **GitHub Action**: Trigger via PR comments or scheduled jobs
4. **Cloud Function**: Deploy as serverless function
5. **CI/CD Pipeline**: Integrate with existing CI/CD systems

## Repository Integration

The orchestrator works with the `debashishmitra/poc-agentic-sdlc` repository:

- **Reads from**: GitHub API (issues, code)
- **Writes to**: GitHub API (branches, files, PRs, comments)
- **Branch Naming**:
  - `design/STORY-{N}` for design docs
  - `feature/STORY-{N}` for implementation
- **File Paths**:
  - `docs/designs/Design-STORY-{N}.md` for design docs
  - `src/main/java/...` for implementation code
  - `src/test/java/...` for test code

## Summary

The Agentic Workflow Orchestrator demonstrates a complete, working implementation of AI-powered SDLC automation. It:

- Reads and understands requirements (GitHub issues)
- Designs solutions using AI (Claude)
- Implements code automatically
- Reviews code for quality
- Creates PRs and comments for human review

All while maintaining clear separation of concerns, comprehensive error handling, and rich console output for visibility into each step.

---

**Version**: 0.1.0
**Status**: Proof of Concept
**Last Updated**: 2026-03-12
