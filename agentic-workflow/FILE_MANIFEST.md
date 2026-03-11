# File Manifest

Complete list of all files created in the Agentic Workflow Orchestrator project.

## Table of Contents

- [Directory Structure](#directory-structure)
- [File Descriptions](#file-descriptions)
- [File Statistics](#file-statistics)
- [Dependencies](#dependencies)
- [Module Relationships](#module-relationships)
- [File Sizes (Approximate)](#file-sizes-approximate)
- [File Creation Order](#file-creation-order)
- [Usage of Each File](#usage-of-each-file)
- [Integration Points](#integration-points)
- [Extensibility Points](#extensibility-points)

## Directory Structure

```
agentic-workflow/
├── config/
│   ├── __init__.py
│   └── settings.py
├── agents/
│   ├── __init__.py
│   ├── story_reader.py
│   ├── design_agent.py
│   ├── implementation_agent.py
│   └── review_agent.py
├── utils/
│   ├── __init__.py
│   ├── github_client.py
│   ├── claude_client.py
│   └── formatting.py
├── orchestrator.py
├── run.py
├── __init__.py
├── requirements.txt
├── README.md
├── QUICKSTART.md
├── ARCHITECTURE.md
├── FILE_MANIFEST.md (this file)
└── INDEX.md
```

## File Descriptions

### Core Files

#### `run.py` (100 lines)
**CLI Entry Point**
- Argument parsing for `--issue`, `--phase`, `--pr`, `--verbose`
- Main entry point for the application
- Error handling and exit codes
- Usage examples in docstring

#### `orchestrator.py` (300 lines)
**Main Workflow Coordinator**
- `WorkflowOrchestrator` class that manages all phases
- Methods: `run_story_phase()`, `run_design_phase()`, `run_implementation_phase()`, `run_review_phase()`
- `run_full_pipeline()` for complete SDLC workflow
- `run_phase()` for individual phase execution
- Workflow state management

### Configuration

#### `config/settings.py` (80 lines)
**Configuration Management**
- Environment variable handling
- Configuration constants (GitHub, Anthropic, API settings)
- `validate_config()` function to check required env vars
- Model selection and token limits

#### `config/__init__.py` (10 lines)
**Config Package Init**
- Exports all configuration constants
- Makes `from config import *` work

### Agents

#### `agents/story_reader.py` (200 lines)
**Story Reading Agent**
- `StoryReaderAgent` class
- `read_story(issue_number)` - Main entry point
- `parse_issue(issue_data)` - Extracts structured story data
- `_extract_section()` - Parses specific sections from issue body
- `_extract_story_points()` - Extracts story point estimates
- `_extract_priority()` - Determines priority level
- Console output formatting

#### `agents/design_agent.py` (350 lines)
**Design Generation Agent**
- `DesignAgent` class
- `generate_design(story_data)` - Generates design doc using Claude
- `read_repo_context()` - Reads existing codebase for patterns
- `commit_design_doc()` - Commits design to branch
- `create_design_pr()` - Creates pull request for design review
- `add_issue_comment()` - Links design PR to original issue
- `run()` - End-to-end design phase

#### `agents/implementation_agent.py` (450 lines)
**Code Generation Agent**
- `ImplementationAgent` class
- `generate_implementation()` - Generates all code components
- Multiple prompt builders for different code types:
  - `_build_entity_prompt()` - JPA entity code
  - `_build_repository_prompt()` - Spring Data repository
  - `_build_service_prompt()` - Business logic service
  - `_build_controller_prompt()` - REST controller
  - `_build_unit_test_prompt()` - JUnit 5 tests
  - `_build_integration_test_prompt()` - Spring Boot integration tests
- `extract_classes_from_code()` - Parses generated code to extract class names
- `commit_implementation()` - Commits all files to feature branch
- `create_implementation_pr()` - Creates implementation PR
- `run()` - End-to-end implementation phase

#### `agents/review_agent.py` (250 lines)
**Code Review Agent**
- `ReviewAgent` class
- `get_pr_context()` - Fetches PR info and diff from GitHub
- `analyze_pr()` - Sends code to Claude for review
- `parse_review()` - Extracts approval status from review text
- `post_review()` - Posts review to GitHub PR
- Fallback to regular comments if review API fails
- `run()` - End-to-end review phase

#### `agents/__init__.py` (10 lines)
**Agents Package Init**
- Exports all agent classes
- Makes `from agents import *` work

### Utilities

#### `utils/github_client.py` (350 lines)
**GitHub API Client**
- `GitHubClient` class wrapping GitHub REST API
- Methods for issues: `get_issue()`, `add_issue_comment()`, `update_issue_labels()`
- Methods for branches: `create_branch()`, `get_commits()`
- Methods for files: `get_file_content()`, `create_or_update_file()`
- Methods for PRs: `create_pull_request()`, `get_pr()`, `get_pr_diff()`
- Methods for reviews: `create_pr_review()`, `create_pr_review_comment()`
- Internal `_make_request()` for all HTTP operations
- Proper error handling and token authentication

#### `utils/claude_client.py` (250 lines)
**Claude API Client**
- `ClaudeClient` class wrapping Anthropic Claude API
- `generate()` - Basic text generation
- `generate_with_context()` - Generation with file context
- `stream_generate()` - Streaming responses (experimental)
- `count_tokens()` - Token estimation
- Proper API endpoint and header handling
- Error handling for API failures

#### `utils/formatting.py` (150 lines)
**Console Output Formatting**
- `Colors` class with ANSI color codes
- `print_agent_header()` - Print agent start messages
- `print_step()` - Print progress steps with emojis
- `print_success()` - Print success messages
- `print_error()` - Print error messages
- `print_warning()` - Print warning messages
- `print_info()` - Print info messages
- `print_section()` - Print section headers
- `print_result()` - Print key-value pairs
- `print_summary()` - Print formatted summary tables
- `print_banner()` - Print centered banner messages

#### `utils/__init__.py` (20 lines)
**Utils Package Init**
- Exports all utility classes and functions
- Makes `from utils import *` work

### Root Package Files

#### `__init__.py` (10 lines)
**Root Package Init**
- Version info
- Author info
- Package description

### Documentation

#### `README.md` (600 lines)
**Complete Documentation**
- Installation instructions
- Usage examples for all phases
- Configuration guide
- Detailed agent descriptions
- API client documentation
- Troubleshooting section
- Contributing guidelines
- Architecture overview

#### `QUICKSTART.md` (400 lines)
**Quick Start Guide**
- 5-minute setup instructions
- Step-by-step workflow examples
- Understanding console output
- Common troubleshooting
- Tips and tricks
- Performance notes
- Sample GitHub issue format

#### `ARCHITECTURE.md` (500 lines)
**Architecture & Design Document**
- System architecture diagram
- Component hierarchy
- Data flow diagrams
- State management details
- API integration details
- Module dependencies
- Execution flow diagram
- Thread safety notes
- Error recovery strategies
- Performance optimization ideas
- Scalability considerations
- Security architecture

#### `FILE_MANIFEST.md` (this file)
**File Manifest**
- Complete file listing
- File descriptions
- Line counts
- Purpose of each file

### Configuration Files

#### `requirements.txt` (3 lines)
**Python Dependencies**
- requests>=2.28.0 (HTTP client)
- anthropic>=0.18.0 (Optional, SDK)
- python-dotenv>=0.19.0 (Environment loading)

## File Statistics

### By Type
- Python Files (.py): 13 files (2000+ lines)
- Documentation (.md): 4 files (1600+ lines)
- Configuration (.txt): 1 file (3 lines)
- Total: 18 files

### By Category
- Agents: 4 files + 1 init = 5 files
- Utilities: 3 files + 1 init = 4 files
- Configuration: 1 file + 1 init = 2 files
- Core: 2 files (run.py, orchestrator.py)
- Root: 1 init file
- Documentation: 4 files

### Code Metrics
- Agent Code: ~1000 lines
- Utility Code: ~750 lines
- Configuration: ~100 lines
- Core Orchestrator: ~300 lines
- CLI: ~100 lines
- Documentation: ~1600 lines
- **Total: ~3850 lines**

## Dependencies

### External Libraries
- **requests** (HTTP client)
  - Used in: GitHubClient, ClaudeClient
  - Purpose: Make HTTP requests to GitHub and Anthropic APIs

- **anthropic** (Optional SDK)
  - Not used in current implementation
  - Could be used as alternative to raw requests
  - Included for future enhancement

- **python-dotenv**
  - Used for: Loading .env files
  - Purpose: Easy environment variable management

### Standard Library
- os (environment variables)
- re (regex for parsing)
- json (JSON handling)
- requests (HTTP)
- time (timing)
- pathlib (file paths)
- typing (type hints)
- argparse (CLI arguments)
- base64 (encoding for GitHub API)
- datetime (timestamps)

## Module Relationships

```
run.py
  └─ orchestrator.py
      ├─ agents/story_reader.py
      │   └─ utils/github_client.py
      ├─ agents/design_agent.py
      │   ├─ utils/github_client.py
      │   ├─ utils/claude_client.py
      │   └─ utils/formatting.py
      ├─ agents/implementation_agent.py
      │   ├─ utils/github_client.py
      │   ├─ utils/claude_client.py
      │   └─ utils/formatting.py
      ├─ agents/review_agent.py
      │   ├─ utils/github_client.py
      │   ├─ utils/claude_client.py
      │   └─ utils/formatting.py
      └─ config/settings.py
```

## File Sizes (Approximate)

| File | Lines | Size |
|------|-------|------|
| orchestrator.py | 300 | 10 KB |
| design_agent.py | 350 | 12 KB |
| implementation_agent.py | 450 | 15 KB |
| github_client.py | 350 | 12 KB |
| claude_client.py | 250 | 8 KB |
| review_agent.py | 250 | 9 KB |
| story_reader.py | 200 | 7 KB |
| run.py | 100 | 3 KB |
| formatting.py | 150 | 5 KB |
| settings.py | 80 | 3 KB |
| All __init__.py | 50 | 2 KB |
| README.md | 600 | 25 KB |
| QUICKSTART.md | 400 | 15 KB |
| ARCHITECTURE.md | 500 | 20 KB |
| requirements.txt | 3 | < 1 KB |
| **Total** | **3850+** | **150+ KB** |

## File Creation Order

The files were created in this logical order:

1. **Configuration Layer**
   - config/settings.py
   - config/__init__.py

2. **Utilities Layer**
   - utils/github_client.py
   - utils/claude_client.py
   - utils/formatting.py
   - utils/__init__.py

3. **Agents Layer**
   - agents/story_reader.py
   - agents/design_agent.py
   - agents/implementation_agent.py
   - agents/review_agent.py
   - agents/__init__.py

4. **Core Orchestrator**
   - orchestrator.py
   - run.py
   - __init__.py

5. **Configuration & Dependencies**
   - requirements.txt

6. **Documentation**
   - README.md
   - QUICKSTART.md
   - ARCHITECTURE.md
   - FILE_MANIFEST.md

## Usage of Each File

### Day-to-Day Usage
- **run.py**: Entry point - users run this daily
- **orchestrator.py**: Core logic - indirect usage via run.py
- **agents/*.py**: Indirectly used via orchestrator
- **utils/*.py**: Indirectly used by agents
- **config/settings.py**: Read at startup

### Development
- **README.md**: Reference for detailed API docs
- **QUICKSTART.md**: Reference for getting started
- **ARCHITECTURE.md**: Reference for understanding design
- **requirements.txt**: To install dependencies

### Deployment
- All .py files needed
- config/ directory needed
- agents/ directory needed
- utils/ directory needed
- requirements.txt needed

### Optional
- Documentation files (.md) not required for operation
- Only needed for understanding and development

## Integration Points

### With GitHub
- GitHubClient uses GitHub REST API v3
- Authenticates with GITHUB_TOKEN env var
- Reads/writes to debashishmitra/poc-agentic-sdlc repo

### With Anthropic Claude
- ClaudeClient uses Claude API endpoint
- Authenticates with ANTHROPIC_API_KEY env var
- Uses claude-opus-4-6 model

### With User
- run.py provides CLI interface
- formatting.py provides console feedback
- GitHub issues serve as input
- GitHub PRs serve as output

## Extensibility Points

To extend the system:

1. **Add New Agent**
   - Create file in agents/
   - Inherit from agent pattern
   - Add to orchestrator.py
   - Update run.py for CLI support

2. **Add New Utility**
   - Create file in utils/
   - Export from utils/__init__.py
   - Use in agents as needed

3. **Add New Configuration**
   - Update config/settings.py
   - Export from config/__init__.py
   - Use in agents/utils as needed

4. **Change AI Model**
   - Update MODEL in config/settings.py
   - Update system prompts in agents/

5. **Add New Documentation**
   - Create .md files in root
   - Reference from README.md

---

**Total Project**: 18 files, 3850+ lines of code, 1600+ lines of documentation

**Created**: March 6, 2026
**Version**: 0.1.0 POC
