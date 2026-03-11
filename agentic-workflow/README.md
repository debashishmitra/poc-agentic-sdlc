# Agentic SDLC Workflow Orchestrator

A Python-based AI-powered orchestrator that automates the entire software development lifecycle using Claude AI. It reads GitHub issues, generates technical design documents, creates implementation code, and performs automated code reviews.

![Python](https://img.shields.io/badge/Python-3.8+-3776AB?style=flat&logo=python&logoColor=white)
![Claude AI](https://img.shields.io/badge/Claude%20AI-Opus%204.6-D4A574?style=flat&logo=anthropic&logoColor=white)
![GitHub API](https://img.shields.io/badge/GitHub-REST%20API%20v3-181717?style=flat&logo=github&logoColor=white)
![Requests](https://img.shields.io/badge/Requests-2.28+-2D8CFF?style=flat&logo=python&logoColor=white)
![dotenv](https://img.shields.io/badge/python--dotenv-0.19+-ECD53F?style=flat&logo=dotenv&logoColor=black)

## Table of Contents

- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [File Structure](#file-structure)
- [Configuration](#configuration)
- [API Clients](#api-clients)
- [Agents](#agents)
- [How It Works](#how-it-works)
- [Console Output](#console-output)
- [Error Handling](#error-handling)
- [Extensibility](#extensibility)
- [Limitations and Future Enhancements](#limitations-and-future-enhancements)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)

## Architecture

The orchestrator consists of four specialized agents working in a coordinated pipeline:

### Phase 1: Story Reader Agent
- Reads GitHub issues and extracts story data
- Parses acceptance criteria, technical notes, and story points
- Provides structured story information to downstream agents

### Phase 2: Design Agent
- Uses Claude to generate comprehensive technical design documents
- Includes API specifications, data models, architecture diagrams (Mermaid)
- Creates a PR for design review before implementation begins
- Commits design document to a dedicated branch

### Phase 3: Implementation Agent
- Generates production-ready Java/Spring Boot code based on the design
- Creates entity classes, repositories, services, and controllers
- Generates unit tests and integration tests
- Commits all code to a feature branch and creates implementation PR

### Phase 4: Review Agent
- Performs automated code review using Claude
- Analyzes code for quality, security, and best practices
- Posts review comments directly on the PR
- Provides approval status (APPROVED, CHANGES_REQUESTED, COMMENT)

## Installation

### Prerequisites
- Python 3.8+
- GitHub personal access token with repo access
- Anthropic API key
- Python packages: `requests`, `anthropic`, `python-dotenv`

### Setup

1. Navigate to the agentic-workflow directory:
```bash
cd agentic-workflow
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Set environment variables:
```bash
export GITHUB_TOKEN="your_github_token"
export ANTHROPIC_API_KEY="your_anthropic_api_key"
export REPO_LOCAL_PATH="/path/to/repo"  # Optional, defaults to parent directory
```

Alternatively, create a `.env` file:
```
GITHUB_TOKEN=your_github_token
ANTHROPIC_API_KEY=your_anthropic_api_key
REPO_LOCAL_PATH=/path/to/repo
```

## Usage

### Run Full Pipeline
Execute the complete SDLC workflow for an issue:

```bash
python run.py --issue 1
```

This will:
1. Read the issue
2. Generate a design document and create a design PR
3. Generate implementation code and create an implementation PR
4. Perform an automated code review on the implementation PR

### Run Individual Phases

Run only the story reading phase:
```bash
python run.py --issue 1 --phase story
```

Run only the design phase:
```bash
python run.py --issue 1 --phase design
```

Run only the implementation phase:
```bash
python run.py --issue 1 --phase implement
```

Run code review on a PR:
```bash
python run.py --issue 1 --phase review --pr 2
```

### Options

- `--issue` (required): GitHub issue number
- `--phase`: Specific phase to run (story, design, implement, review)
- `--pr`: PR number (required for review phase)
- `--verbose` or `-v`: Enable verbose output with stack traces

## File Structure

```
agentic-workflow/
├── config/
│   ├── __init__.py
│   └── settings.py                # Configuration and environment variables
├── agents/
│   ├── __init__.py
│   ├── story_reader.py           # Story reading and parsing agent
│   ├── design_agent.py           # Design document generation agent
│   ├── implementation_agent.py   # Code generation agent
│   └── review_agent.py           # Automated code review agent
├── utils/
│   ├── __init__.py
│   ├── github_client.py          # GitHub API client
│   ├── claude_client.py          # Anthropic Claude API client
│   └── formatting.py             # Console output formatting
├── orchestrator.py               # Main workflow orchestrator
├── run.py                        # CLI entry point
├── requirements.txt              # Python dependencies
└── README.md                     # This file
```

## Configuration

### settings.py

Core configuration settings:

- `GITHUB_TOKEN`: GitHub personal access token (from env)
- `GITHUB_REPO`: Repository path (default: debashishmitra/poc-agentic-sdlc)
- `ANTHROPIC_API_KEY`: Anthropic API key (from env)
- `MODEL`: Claude model to use (default: claude-opus-4-6)
- `REPO_LOCAL_PATH`: Local repository path for context
- `API_TIMEOUT`: Request timeout in seconds (default: 30)
- `MAX_TOKENS`: Maximum tokens in Claude responses (default: 4096)

## API Clients

### GitHubClient

Provides methods for:
- Reading issues: `get_issue(issue_number)`
- Branch management: `create_branch(branch_name, base)`
- File operations: `get_file_content()`, `create_or_update_file()`
- Pull requests: `create_pull_request()`, `get_pr()`, `get_pr_diff()`
- Comments: `add_issue_comment()`, `create_pr_review()`
- Labels: `update_issue_labels()`

### ClaudeClient

Provides methods for:
- Text generation: `generate(system_prompt, user_prompt, max_tokens)`
- Context-aware generation: `generate_with_context(system_prompt, user_prompt, context_files)`
- Streaming: `stream_generate()` (experimental)
- Token counting: `count_tokens()`

## Agents

### StoryReaderAgent

**Purpose**: Parse GitHub issues into structured story data

**Methods**:
- `read_story(issue_number)`: Read and parse a GitHub issue
- `parse_issue(issue_data)`: Extract story components
- `_extract_section()`: Parse issue sections (acceptance criteria, technical notes)
- `_extract_story_points()`: Extract story point estimate
- `_extract_priority()`: Determine priority level

**Output**: Structured story dictionary with:
- `number`: Issue number
- `title`: Story title
- `description`: Story description
- `acceptance_criteria`: List of acceptance criteria
- `technical_notes`: Technical context
- `story_points`: Story point estimate
- `priority`: Priority level (high/medium/low)

### DesignAgent

**Purpose**: Generate technical design documents using Claude

**Methods**:
- `generate_design(story_data)`: Generate design document
- `commit_design_doc()`: Commit design to a branch
- `create_design_pr()`: Create design review PR
- `run()`: End-to-end design phase

**Deliverables**:
- Technical Design Document including:
  - API specifications
  - Data model changes
  - Architecture diagrams (Mermaid)
  - Service layer design
  - Testing strategy
- Design review PR on GitHub

### ImplementationAgent

**Purpose**: Generate production-ready implementation code

**Methods**:
- `generate_implementation()`: Generate code for all components
- `commit_implementation()`: Commit code to feature branch
- `create_implementation_pr()`: Create implementation PR
- `run()`: End-to-end implementation phase

**Code Generated**:
- Entity/Model classes (JPA)
- Repository interfaces (Spring Data)
- Service classes (business logic)
- REST Controller classes (endpoints)
- Unit tests (JUnit 5, Mockito)
- Integration tests (Spring Boot Test)

### ReviewAgent

**Purpose**: Perform automated code review using Claude

**Methods**:
- `get_pr_context()`: Fetch PR and diff information
- `analyze_pr()`: Analyze code with Claude
- `post_review()`: Post review to GitHub
- `run()`: End-to-end review phase

**Review Coverage**:
- Code quality and maintainability
- Error handling and validation
- Test coverage
- Security concerns
- Spring Boot best practices
- Performance implications

## How It Works

### Full Pipeline Flow

```
Issue #1
   ↓
[Story Reader] → Parses issue, extracts story data
   ↓
[Design Agent] → Generates technical design document
                → Creates design PR for review
   ↓
[Implementation Agent] → Generates Java/Spring Boot code
                      → Creates implementation PR
   ↓
[Review Agent] → Analyzes code quality
               → Posts review comments
               → Sets approval status
```

### Design Phase Details

1. **Context Collection**: Reads existing code from repository to understand patterns
2. **Design Generation**: Uses Claude to generate comprehensive technical design
3. **Documentation**: Creates design document with:
   - Overview and objectives
   - REST API specifications
   - Data model and schema changes
   - Architecture diagrams (Mermaid syntax)
   - Service layer design
   - Testing strategy
   - Implementation notes
4. **Branch Creation**: Creates `design/STORY-{number}` branch
5. **PR Creation**: Creates pull request for design review
6. **Issue Linking**: Adds comment to original issue linking to design PR

### Implementation Phase Details

1. **Code Generation**: Uses Claude to generate:
   - Entity classes with JPA annotations
   - Repository interfaces with custom queries
   - Service classes with business logic
   - REST controllers with proper endpoints
   - Unit tests covering service logic
   - Integration tests covering APIs
2. **Code Quality**: Generated code includes:
   - Proper Spring Boot annotations
   - Dependency injection
   - Error handling and validation
   - Logging statements
   - Comments and documentation
3. **Branch Creation**: Creates `feature/STORY-{number}` branch
4. **PR Creation**: Creates pull request with summary of changes

### Review Phase Details

1. **PR Analysis**: Reviews:
   - Code style and maintainability
   - Error handling strategies
   - Test coverage and quality
   - Security vulnerabilities
   - Spring Boot pattern adherence
   - Performance implications
2. **Review Output**: Posts comprehensive review with:
   - Summary of changes
   - Specific issues identified
   - Suggestions for improvement
   - Approval status (APPROVED/CHANGES_REQUESTED/COMMENT)
3. **GitHub Integration**: Posts review directly on PR for visibility

## Console Output

The orchestrator provides rich, formatted console output:

```
**Agent Headers**: Clearly mark which agent is running
⚙️  Steps: Show progress within each phase
✓ Success: Confirm successful operations
✗ Errors: Display errors with context
━━━ Sections: Organize output by workflow phase
📋 Summaries: Display key results
```

## Error Handling

The orchestrator includes comprehensive error handling:

- **Configuration Validation**: Checks required environment variables on startup
- **API Error Handling**: Gracefully handles GitHub and Claude API failures
- **Fallback Strategies**: Falls back to regular comments if PR reviews fail
- **Phase Independence**: Failure in one phase doesn't prevent manual execution of later phases
- **Detailed Error Messages**: Provides context and suggestions for fixing issues

## Extensibility

The architecture supports easy extension:

### Adding a New Agent

1. Create new agent class inheriting from a base agent pattern
2. Implement required methods (`run()` method and helper methods)
3. Add to orchestrator workflow
4. Update CLI to support new phase

### Custom Configuration

Modify `config/settings.py` to add:
- New API endpoints
- Model selections for different tasks
- Timeout values
- Token limits

### Custom Formatting

Extend `utils/formatting.py` with additional output styles:
- New color schemes
- Additional message types
- Progress indicators

## Limitations and Future Enhancements

### Current Limitations
- Design approval is implicit (no waiting for PR approval)
- Implementation uses mock design context for mid-pipeline runs
- Review status doesn't block pipeline
- No deployment/merge automation

### Planned Enhancements
- GitHub Actions integration for automatic PR checks
- Deployment automation to staging/production
- Design PR approval requirements
- Multi-language code generation support
- Database migration generation
- Documentation generation from code
- Performance benchmarking
- Metrics and reporting dashboard

## Troubleshooting

### Configuration Errors

**Problem**: "GITHUB_TOKEN environment variable is not set"

**Solution**: Set the environment variable:
```bash
export GITHUB_TOKEN="your_token"
```

**Problem**: "ANTHROPIC_API_KEY environment variable is not set"

**Solution**: Set the environment variable:
```bash
export ANTHROPIC_API_KEY="your_key"
```

### API Errors

**Problem**: "GitHub API request failed"

**Solution**:
- Verify GitHub token has repo access
- Check rate limits: `curl -H "Authorization: token YOUR_TOKEN" https://api.github.com/rate_limit`
- Ensure issue number is correct

**Problem**: "Claude API request failed"

**Solution**:
- Verify Anthropic API key is correct
- Check API quota and usage
- Verify internet connectivity

### Code Generation Issues

**Problem**: Generated code has syntax errors

**Solution**:
- Review Claude's output in the created PR
- Check if existing code patterns in repo match expectations
- Verify story description has clear technical requirements

## Contributing

This is a proof-of-concept project. Areas for contribution:
- Additional agent types (deployment, documentation, etc.)
- Language support (Go, Python, JavaScript, etc.)
- Enhanced code generation templates
- Improved error recovery
- Performance optimizations
- Integration with additional tools (Jira, GitLab, etc.)

## License

This project is part of the poc-agentic-sdlc repository.

## Support

For issues or questions:
1. Check this README
2. Review agent implementations for implementation details
3. Check Claude API documentation for model capabilities
4. Check GitHub API documentation for endpoint details

---

**Generated by**: Agentic SDLC Workflow Orchestrator
**Version**: 0.1.0
**Last Updated**: 2026-03-12
