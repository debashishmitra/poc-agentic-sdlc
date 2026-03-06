# Architecture & Design

## System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    Agentic SDLC Orchestrator                      │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────┐
│  GitHub Repo    │
│  (Issues, Code) │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CLI Entry Point                             │
│                        (run.py)                                  │
└────────────────┬────────────────────────────────────────────────┘
                 │
         ┌───────▼──────────────────────────────┐
         │  WorkflowOrchestrator                │
         │  - Coordinates all agents           │
         │  - Manages workflow state            │
         │  - Handles phase execution           │
         └───────┬──────────────────────────────┘
                 │
     ┌───────────┼───────────┬──────────────┐
     │           │           │              │
     ▼           ▼           ▼              ▼
┌─────────┐ ┌─────────┐ ┌──────────────┐ ┌──────────┐
│ Story   │ │ Design  │ │Implementation│ │  Review  │
│ Reader  │ │ Agent   │ │   Agent      │ │  Agent   │
│ Agent   │ │         │ │              │ │          │
└────┬────┘ └────┬────┘ └──────┬───────┘ └────┬─────┘
     │           │             │              │
     │           ▼             ▼              │
     │      ┌──────────────────────┐         │
     │      │ Claude API           │         │
     │      │ (claude-sonnet-4)    │         │
     │      └──────────────────────┘         │
     │                                       │
     │      ┌──────────────────────┐         │
     └─────▶│ GitHub API           │◀────────┘
            │ (REST v3)            │
            └──────────────────────┘
```

## Component Hierarchy

```
orchestrator.py (Main Coordinator)
├── agents/
│   ├── story_reader.py (StoryReaderAgent)
│   │   └── uses: GitHubClient
│   ├── design_agent.py (DesignAgent)
│   │   ├── uses: GitHubClient
│   │   └── uses: ClaudeClient
│   ├── implementation_agent.py (ImplementationAgent)
│   │   ├── uses: GitHubClient
│   │   └── uses: ClaudeClient
│   └── review_agent.py (ReviewAgent)
│       ├── uses: GitHubClient
│       └── uses: ClaudeClient
├── utils/
│   ├── github_client.py (GitHubClient)
│   │   └── uses: requests library
│   ├── claude_client.py (ClaudeClient)
│   │   └── uses: requests library
│   └── formatting.py (Console output formatting)
└── config/
    └── settings.py (Configuration management)
```

## Data Flow Architecture

```
Phase 1: Story Reading
┌────────────────────┐
│ GitHub Issue #N    │
└─────────┬──────────┘
          │ (GET /repos/.../issues/{N})
          ▼
┌────────────────────────────────────────┐
│ Story Reader Agent                     │
│ - Parses issue body                    │
│ - Extracts structured data             │
│ - Analyzes acceptance criteria         │
└─────────┬──────────────────────────────┘
          │
          ▼
   StoryData = {
       number, title, description,
       acceptance_criteria,
       technical_notes,
       story_points, priority
   }


Phase 2: Design Generation
┌────────────────────┐
│ Story Data         │
└─────────┬──────────┘
          │
          ▼
┌────────────────────────────────────┐     ┌──────────────────────┐
│ Design Agent                       │────▶│ Read Repo Context    │
│ - Generates design doc             │     │ (Entity, Service,    │
│ - Creates branch                   │     │  Controller classes) │
│ - Commits document                 │     └──────────────────────┘
│ - Creates PR                       │
└────────┬─────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────────────┐
│ Claude API (Design Generation)                │
│ System: "You are a software architect..."     │
│ Prompt: Story data + design requirements      │
└────────┬──────────────────────────────────────┘
         │
         ▼
Design Document = {
    overview, objectives,
    api_specs, data_models,
    architecture_diagrams,
    service_design,
    testing_strategy,
    implementation_notes
}


Phase 3: Implementation
┌──────────────────────────┐
│ Story Data + Design Doc  │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────────────┐     ┌──────────────────────┐
│ Implementation Agent             │────▶│ Read Repo Patterns   │
│ - Generates 6+ Java files        │     │ (Existing code       │
│ - Creates feature branch         │     │  patterns to match)  │
│ - Commits code                   │     └──────────────────────┘
│ - Creates PR                     │
└──────┬───────────────────────────┘
       │
       ├─▶ Claude API (Entity Generation)
       ├─▶ Claude API (Repository Generation)
       ├─▶ Claude API (Service Generation)
       ├─▶ Claude API (Controller Generation)
       ├─▶ Claude API (Unit Test Generation)
       └─▶ Claude API (Integration Test Generation)
           │
           ▼
Implementation Code = {
    Entity.java,
    Repository.java,
    Service.java,
    Controller.java,
    ServiceTest.java,
    ControllerTest.java
}


Phase 4: Code Review
┌─────────────────────────────┐
│ Implementation PR           │
└──────────┬──────────────────┘
           │ (GET /repos/.../pulls/{N} + diff)
           ▼
┌──────────────────────────────────┐
│ Review Agent                     │
│ - Fetches PR and diff            │
│ - Analyzes code quality          │
│ - Creates review                 │
└──────┬───────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│ Claude API (Code Review)                 │
│ System: "You are a code reviewer..."     │
│ Prompt: PR title + diff                  │
└──────┬───────────────────────────────────┘
       │
       ▼
Code Review = {
    summary,
    issues,
    suggestions,
    approval_status
}
```

## State Management

```
WorkflowOrchestrator.workflow_state = {
    "story_data": {
        "number": 1,
        "title": "...",
        "description": "...",
        "acceptance_criteria": "...",
        "technical_notes": "...",
        "story_points": 8,
        "priority": "high"
    },

    "design_result": {
        "design_content": "# Technical Design Document\n...",
        "branch": "design/STORY-1",
        "file": "docs/designs/Design-STORY-1.md",
        "pr_number": 12,
        "pr_url": "https://github.com/.../pull/12"
    },

    "implementation_result": {
        "implementation_code": {
            "entity": "public class User { ... }",
            "repository": "public interface UserRepository { ... }",
            "service": "public class UserService { ... }",
            "controller": "public class UserController { ... }",
            "unit_tests": "public class UserServiceTest { ... }",
            "integration_tests": "public class UserControllerTest { ... }"
        },
        "branch": "feature/STORY-1",
        "files": {
            "entity": { "class": "User", "file": "src/main/java/.../User.java" },
            ...
        },
        "pr_number": 13,
        "pr_url": "https://github.com/.../pull/13"
    },

    "review_result": {
        "pr_number": 13,
        "review_text": "# Code Review\n...",
        "approval_status": "APPROVED",
        "posted": true
    }
}
```

## API Integration Details

### GitHub API Flow

```
StoryReaderAgent
├─ GET /repos/{owner}/{repo}/issues/{issue_number}
│  └─ Returns: issue data with title, body, labels
│
DesignAgent
├─ GET /repos/{owner}/{repo}/git/refs/heads/{branch}
│  └─ Returns: branch reference (for base branch SHA)
├─ POST /repos/{owner}/{repo}/git/refs
│  └─ Create: design/STORY-{N} branch
├─ GET /repos/{owner}/{repo}/contents/{path}
│  └─ Returns: file content (for context reading)
├─ PUT /repos/{owner}/{repo}/contents/{path}
│  └─ Create/Update: Design document file
├─ POST /repos/{owner}/{repo}/pulls
│  └─ Create: Design review PR
└─ POST /repos/{owner}/{repo}/issues/{issue_number}/comments
   └─ Create: Issue comment linking to PR
│
ImplementationAgent
├─ [Same branch creation pattern as DesignAgent]
├─ PUT /repos/{owner}/{repo}/contents/{path}
│  └─ Create/Update: 6+ Java files
├─ POST /repos/{owner}/{repo}/pulls
│  └─ Create: Implementation PR
│
ReviewAgent
├─ GET /repos/{owner}/{repo}/pulls/{pr_number}
│  └─ Returns: PR data
├─ GET /repos/{owner}/{repo}/pulls/{pr_number}
│  └─ (with Accept: application/vnd.github.v3.diff)
│  └─ Returns: Diff content
├─ POST /repos/{owner}/{repo}/pulls/{pr_number}/reviews
│  └─ Create: Code review
└─ POST /repos/{owner}/{repo}/issues/{pr_number}/comments
   └─ Fallback: Regular comment if review fails
```

### Claude API Flow

```
Design Generation
├─ POST https://api.anthropic.com/v1/messages
│  ├─ headers: {
│  │    "anthropic-version": "2023-06-01",
│  │    "x-api-key": "sk-ant-...",
│  │    "content-type": "application/json"
│  │ }
│  └─ body: {
│       "model": "claude-sonnet-4-20250514",
│       "max_tokens": 6000,
│       "system": "You are a software architect...",
│       "messages": [{
│           "role": "user",
│           "content": "Generate design for: story #1 + context"
│       }]
│  }
│
Implementation Generation
├─ POST https://api.anthropic.com/v1/messages
│  └─ Called 6 times (entity, repo, service, controller, tests)
│  └─ Each with max_tokens: 3000
│
Code Review
├─ POST https://api.anthropic.com/v1/messages
│  ├─ max_tokens: 4000
│  └─ Prompt includes PR diff in full
```

## Module Dependencies

```
run.py
├─ orchestrator.py
│  ├─ agents/story_reader.py
│  │  └─ utils/github_client.py
│  │     └─ requests
│  ├─ agents/design_agent.py
│  │  ├─ utils/github_client.py
│  │  ├─ utils/claude_client.py
│  │  │  └─ requests
│  │  └─ utils/formatting.py
│  ├─ agents/implementation_agent.py
│  │  ├─ utils/github_client.py
│  │  ├─ utils/claude_client.py
│  │  └─ utils/formatting.py
│  ├─ agents/review_agent.py
│  │  ├─ utils/github_client.py
│  │  ├─ utils/claude_client.py
│  │  └─ utils/formatting.py
│  └─ config/settings.py
│
config/settings.py
├─ os (environment variables)
├─ pathlib.Path
└─ [No external dependencies]

utils/github_client.py
├─ requests
├─ json
├─ config/settings.py
└─ [Standard library imports]

utils/claude_client.py
├─ requests
├─ json
├─ config/settings.py
└─ [Standard library imports]

utils/formatting.py
└─ [Only standard library]
```

## Execution Flow Diagram

```
START: python run.py --issue 1
│
├─ Parse arguments
│
├─ Validate config (check env vars)
│
└─ Create WorkflowOrchestrator
   │
   ├─ Phase 1: run_story_phase(1)
   │  │
   │  └─ StoryReaderAgent.read_story(1)
   │     ├─ GitHubClient.get_issue(1)
   │     │  └─ requests.get("https://api.github.com/repos/.../issues/1")
   │     │
   │     └─ Parse issue body
   │        └─ Extract: title, criteria, notes, points, priority
   │           └─ Returns: StoryData dict
   │
   ├─ Phase 2: run_design_phase(story_data)
   │  │
   │  └─ DesignAgent.run(story_data)
   │     │
   │     ├─ read_repo_context()
   │     │  └─ GitHubClient.get_file_content() x4
   │     │
   │     ├─ generate_design(story_data)
   │     │  └─ ClaudeClient.generate(system_prompt, user_prompt)
   │     │     └─ requests.post("https://api.anthropic.com/v1/messages")
   │     │
   │     ├─ commit_design_doc(design_content, story_number)
   │     │  ├─ GitHubClient.create_branch("design/STORY-1")
   │     │  └─ GitHubClient.create_or_update_file(
   │     │     "docs/designs/Design-STORY-1.md",
   │     │     design_content,
   │     │     "Design: Technical design for STORY-1",
   │     │     "design/STORY-1"
   │     │  )
   │     │
   │     ├─ create_design_pr(story_number, design_content)
   │     │  └─ GitHubClient.create_pull_request(
   │     │     "Design: Technical design for STORY-1",
   │     │     pr_body,
   │     │     "design/STORY-1",
   │     │     "main"
   │     │  )
   │     │
   │     └─ add_issue_comment(story_number, pr_number)
   │        └─ GitHubClient.add_issue_comment(1, "Design PR #12")
   │
   ├─ Phase 3: run_implementation_phase(story_data, design_result)
   │  │
   │  └─ ImplementationAgent.run(story_data, design_content)
   │     │
   │     ├─ read_repo_patterns()
   │     │  └─ GitHubClient.get_file_content() x6
   │     │
   │     ├─ generate_implementation(story_data, design_content)
   │     │  ├─ ClaudeClient.generate() → Entity code
   │     │  ├─ ClaudeClient.generate() → Repository code
   │     │  ├─ ClaudeClient.generate() → Service code
   │     │  ├─ ClaudeClient.generate() → Controller code
   │     │  ├─ ClaudeClient.generate() → Unit tests
   │     │  └─ ClaudeClient.generate() → Integration tests
   │     │
   │     ├─ commit_implementation(impl_code, story_number)
   │     │  ├─ GitHubClient.create_branch("feature/STORY-1")
   │     │  └─ GitHubClient.create_or_update_file() x6
   │     │
   │     └─ create_implementation_pr(story_number, files)
   │        └─ GitHubClient.create_pull_request(...)
   │
   ├─ Phase 4: run_review_phase(pr_number)
   │  │
   │  └─ ReviewAgent.run(13)
   │     │
   │     ├─ get_pr_context(13)
   │     │  ├─ GitHubClient.get_pr(13)
   │     │  └─ GitHubClient.get_pr_diff(13)
   │     │
   │     ├─ analyze_pr(pr_context)
   │     │  └─ ClaudeClient.generate(review_system, review_prompt)
   │     │     └─ requests.post("https://api.anthropic.com/v1/messages")
   │     │
   │     ├─ parse_review(review_text)
   │     │  └─ Extract: approval_status from review
   │     │
   │     └─ post_review(pr_number, review_text, status)
   │        ├─ GitHubClient.create_pr_review(13, review, "APPROVED")
   │        └─ [Fallback] GitHubClient.add_issue_comment(13, review)
   │
   └─ Print workflow summary
      └─ Print final status and timings

END: Exit with status code
```

## Thread Safety & Concurrency

**Current Design**: Single-threaded, sequential execution
- Each phase completes before next begins
- No concurrent API calls
- State flows forward through method parameters
- Safe for multiple independent workflow runs (different issues)

**Future Enhancement**: Parallel agent execution
- Design and review phases could run in parallel for different PRs
- Claude API calls could be batched
- GitHub API operations support concurrent writes to different branches

## Error Recovery

```
If Phase 1 (Story) fails:
└─ Entire pipeline stops
   └─ User must fix issue and retry

If Phase 2 (Design) fails:
├─ Branch created but incomplete
└─ User can review PR, fix, or restart

If Phase 3 (Implementation) fails:
├─ Design PR already exists
├─ Feature branch created but incomplete
└─ User can review PR, fix, or restart

If Phase 4 (Review) fails:
├─ Implementation PR already exists
├─ Tries fallback: post as comment instead
└─ User can see feedback on PR

If any Phase succeeds:
└─ Results available for next phase
   └─ Can rerun later with context
```

## Performance Optimization

### Current Bottlenecks
1. Claude API response time (30-60 seconds per call)
2. Multiple sequential Claude calls in implementation phase
3. GitHub API rate limits (60 requests/min unauthenticated, 5000/hr authenticated)

### Optimization Opportunities
1. **Batch Claude Calls**: Use streaming for real-time responses
2. **Cache Context**: Store repo patterns to avoid repeated API calls
3. **Parallel Generation**: Generate multiple code components in parallel
4. **Token Optimization**: Trim context files to essential sections

### Implementation Notes
- Token counting is approximate (4 chars per token)
- Max tokens set conservatively to stay under limits
- No streaming implemented yet (can be added)
- GitHub API calls cached per phase run

## Scalability Considerations

```
Current: 1 orchestrator = 1 pipeline run
├─ Sequential execution
├─ ~5 minute runtime
└─ Single machine

Scalable: Multiple orchestrators
├─ One per issue/PR
├─ Queue system for job management
├─ Distributed caching
├─ Load balancing across multiple machines
└─ Message queue (SQS, Kafka) for async processing

With GitHub Actions:
├─ Trigger on issue creation
├─ Run in cloud
├─ Automatic PR creation
└─ Status checks in PRs
```

## Security Architecture

```
Secrets Management:
├─ GITHUB_TOKEN
│  └─ Read from environment
│  └─ Passed via Authorization header
│  └─ Never logged or hardcoded
│
├─ ANTHROPIC_API_KEY
│  └─ Read from environment
│  └─ Passed via x-api-key header
│  └─ Never logged or hardcoded
│
└─ Code Review
   └─ Claude reviews all generated code
   └─ Human review on GitHub
   └─ Can set branch protections

API Security:
├─ HTTPS for all external calls
├─ Token-based authentication
├─ No sensitive data in URLs
└─ GitHub + Anthropic handles encryption

Repository Security:
├─ Separate branches for each phase
├─ Branch protection rules (recommended)
├─ PR reviews before merge
└─ Audit trail of all changes
```

---

**This document provides a comprehensive view of the orchestrator's design, data flows, and architecture.**
