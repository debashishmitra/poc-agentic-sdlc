# Quick Start Guide

Get the Agentic Workflow Orchestrator up and running in minutes.

## 1. Prerequisites

- Python 3.8 or higher
- GitHub personal access token (with `repo` scope)
- Anthropic API key

## 2. Install Dependencies

```bash
cd agentic-workflow
pip install -r requirements.txt
```

## 3. Set Environment Variables

### Option A: Using Environment Variables
```bash
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxx"
export ANTHROPIC_API_KEY="sk-ant-xxxxxxxxxxxxxxxxxxxx"
export REPO_LOCAL_PATH="/path/to/poc-agentic-sdlc"  # Optional
```

### Option B: Using .env File
Create a `.env` file in the `agentic-workflow` directory:
```
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxxxxxxxxx
REPO_LOCAL_PATH=/path/to/poc-agentic-sdlc
```

The `.env` file is loaded automatically by `python-dotenv` when the orchestrator starts.

## 4. Run the Orchestrator

### Full Pipeline (Recommended for First Run)
```bash
python run.py --issue 1
```

This will:
1. Read issue #1 from GitHub
2. Generate a technical design document
3. Generate implementation code
4. Perform automated code review

### View Results
The orchestrator will:
- Print progress to console
- Create branches for each phase
- Create pull requests for design and implementation
- Post code review comments on PRs

### Individual Phases
```bash
# Read and parse story
python run.py --issue 1 --phase story

# Generate design document
python run.py --issue 1 --phase design

# Generate implementation code
python run.py --issue 1 --phase implement

# Review a PR
python run.py --issue 1 --phase review --pr 2
```

## 5. Example Workflow

### Step 1: Check Your GitHub Issue
```bash
# Visit: https://github.com/debashishmitra/poc-agentic-sdlc/issues/1
# Make sure it has:
# - Clear title
# - Description section
# - Acceptance Criteria section (optional)
# - Technical Notes section (optional)
```

### Step 2: Run Design Phase Only
```bash
python run.py --issue 1 --phase design
```

Expected output:
```
==============================================================================
🤖 DESIGN AGENT
Reading issue #1

⚙️  Fetching issue from GitHub...
✓ Issue fetched: 'Add User Authentication' (8 points, high priority)
⚙️  Reading repository context...
✓ Context loaded (4 files)
⚙️  Preparing design prompt...
⚙️  Calling Claude API to generate design...
✓ Design document generated successfully

⚙️  Creating design branch...
✓ Branch created: design/STORY-1
⚙️  Committing design document...
✓ Design document committed to docs/designs/Design-STORY-1.md
⚙️  Creating design review pull request...
✓ Design PR created: #12
⚙️  Adding design link comment to issue...
✓ Comment added to issue

📋 Design Phase Complete
  Branch: design/STORY-1
  File: docs/designs/Design-STORY-1.md
  PR Number: 12
```

### Step 3: Review Design PR
Go to GitHub and review the design PR. You can:
- Add comments
- Request changes
- Approve the design

### Step 4: Run Implementation Phase
```bash
python run.py --issue 1 --phase implement
```

This will generate:
- Entity classes
- Repository interfaces
- Service classes
- REST controllers
- Unit tests
- Integration tests

### Step 5: Review Code and PR
Check the implementation PR on GitHub:
```
https://github.com/debashishmitra/poc-agentic-sdlc/pull/13
```

### Step 6: Run Code Review
```bash
python run.py --issue 1 --phase review --pr 13
```

The review agent will:
- Analyze the code
- Post review comments
- Provide approval recommendation

## 6. Understanding the Output

### Color Codes
- **Cyan**: Agent headers and section dividers
- **Blue**: Progress steps (⚙️)
- **Green**: Success messages (✓)
- **Red**: Error messages (✗)
- **Yellow**: Warnings and summaries

### Console Messages
```
🤖 Agent Name          - Which agent is running
━━━ PHASE X: NAME      - Workflow phase
⚙️  Task description   - Current operation
✓ Success message      - Completed operation
✗ Error message        - Failed operation
📋 Summary             - Results table
```

## 7. Troubleshooting

### GitHub API Errors
**Problem**: "GitHub API request failed"

**Solutions**:
- Check GITHUB_TOKEN is valid: `curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user`
- Verify token has `repo` scope
- Check issue number exists

### Claude API Errors
**Problem**: "Claude API request failed"

**Solutions**:
- Verify ANTHROPIC_API_KEY is correct
- Check you have API quota available
- Verify internet connectivity

### Import Errors
**Problem**: "ModuleNotFoundError: No module named 'config'"

**Solutions**:
- Run from the `agentic-workflow` directory
- Verify Python path: `echo $PYTHONPATH`
- Reinstall dependencies: `pip install -r requirements.txt`

## 8. Next Steps

Once you're comfortable with the basic workflow:

1. **Explore Generated Code**: Check the files created in the feature branches
2. **Customize Design Prompts**: Edit `design_agent.py` system prompts
3. **Adjust Code Generation**: Modify prompts in `implementation_agent.py`
4. **Enhance Reviews**: Customize review criteria in `review_agent.py`

## 9. Architecture Overview

```
GitHub Issue
    ↓
[Story Reader] - Parses issue into structured data
    ↓
[Design Agent] - Generates technical design doc
    ├─ Creates branch: design/STORY-{N}
    ├─ Commits doc: docs/designs/Design-STORY-{N}.md
    └─ Creates PR for review
    ↓
[Implementation Agent] - Generates code based on design
    ├─ Creates branch: feature/STORY-{N}
    ├─ Generates: entities, repos, services, controllers
    ├─ Generates: unit tests, integration tests
    └─ Creates PR for review
    ↓
[Review Agent] - Performs code review
    ├─ Analyzes code quality
    ├─ Checks Spring Boot patterns
    ├─ Reviews security & performance
    └─ Posts review on PR
```

## 10. Key Files

| File | Purpose |
|------|---------|
| `run.py` | CLI entry point |
| `orchestrator.py` | Main workflow coordinator |
| `agents/story_reader.py` | Parse GitHub issues |
| `agents/design_agent.py` | Generate design docs |
| `agents/implementation_agent.py` | Generate code |
| `agents/review_agent.py` | Perform code review |
| `utils/github_client.py` | GitHub API wrapper |
| `utils/claude_client.py` | Claude API wrapper |
| `config/settings.py` | Configuration & env vars |

## 11. Sample Issue Format

For best results, format your GitHub issues like this:

```markdown
# Feature Title

## Description
What this feature should accomplish...

## Acceptance Criteria
- [ ] Criteria 1
- [ ] Criteria 2
- [ ] Criteria 3

## Technical Notes
- Use repository pattern
- Implement proper error handling
- Add logging

## Story Points
5
```

## 12. Tips & Tricks

### Running Multiple Issues
```bash
for issue in 1 2 3; do
  python run.py --issue $issue
done
```

### Verbose Output for Debugging
```bash
python run.py --issue 1 --verbose
```

### Combining Phases Manually
```bash
# Generate design
python run.py --issue 1 --phase design

# Later, after design approval...
# Generate implementation
python run.py --issue 1 --phase implement

# Later, review the implementation PR
python run.py --issue 1 --phase review --pr 13
```

## 13. Performance Notes

- **Story Reading**: 1-2 seconds
- **Design Generation**: 30-60 seconds (depends on Claude API)
- **Implementation Generation**: 60-120 seconds (4 major components)
- **Code Review**: 30-60 seconds
- **Total Pipeline**: 2-5 minutes

## 14. Support Resources

- GitHub API: https://docs.github.com/en/rest
- Anthropic Claude: https://docs.anthropic.com/en/docs/about-claude/models/overview
- Spring Boot: https://spring.io/projects/spring-boot

## Getting Help

1. Check the main README.md for detailed documentation
2. Review error messages and stack traces
3. Check GitHub/Anthropic API documentation
4. Verify environment variables are set correctly
5. Check internet connectivity

---

**Ready to go!** Run `python run.py --issue 1` to get started.
