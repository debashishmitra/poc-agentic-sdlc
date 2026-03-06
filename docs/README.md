# THD Order Management Service - Documentation Hub

This directory serves as the Confluence alternative for the AI Agentic SDLC POC.

## Structure

```
docs/
├── README.md                          # This file (Wiki Home)
├── Architecture-Overview.md           # System architecture
├── API-Documentation.md               # API reference
├── designs/                           # Technical Design Documents
│   └── README.md                      # Design document index
└── templates/                         # Document templates
    └── Design-Document-Template.md    # TDD template
```

## How It Works

1. **AI Design Agent** reads a GitHub Issue (story) and generates a Technical Design Document in `docs/designs/`
2. **Human Architect** reviews and approves the design via PR
3. **AI Coding Agent** implements the approved design
4. **AI Documentation Agent** updates docs after deployment

---

*POC for AI Agentic SDLC Workflows at The Home Depot*
