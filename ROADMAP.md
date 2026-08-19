# ForgePilot AI Roadmap

## M0 — Green Foundation
- Java 21 / Spring Boot service
- health and platform-status endpoints
- unit/integration test baseline
- Docker image
- GitHub Actions CI
- EC2 deployment workflow with health verification

## M1 — Workspace + Projects
- project CRUD
- workspace model
- PostgreSQL persistence
- project settings and visibility
- audit events

## M2 — AI Chat, Plan Mode, Build Mode
- chat sessions
- prompt/context model
- model gateway
- planner agent
- build agent
- structured plans and execution events

## M3 — Files + Code Workspace
- generated project filesystem abstraction
- file tree/search/read/write
- Monaco editor integration
- targeted file/code context
- attachments

## M4 — Sandbox + Live Preview
- isolated execution sandbox
- dependency install/build/run tools
- runtime logs
- preview URL/proxy
- resource/network limits

## M5 — Agentic Build + Repair
- orchestrator
- frontend/backend/database/test agents
- build validation
- automated repair loop
- reviewer agent
- parallel subagents

## M6 — Versioning + GitHub
- snapshot/version history
- diff/revert
- Git commit/branch/push/pull
- two-way GitHub synchronization

## M7 — Backend Platform
- PostgreSQL schema generation/migrations
- authentication and RBAC generation
- object storage
- secrets management
- server functions / API generation

## M8 — Visual Builder
- DOM/component selection bridge
- visual property inspector
- text/style/layout edits
- comments/annotations
- responsive preview

## M9 — Validation + Security
- unit/integration/browser testing
- dependency and secret scanning
- SAST/security agent
- policy gates
- deployment readiness report

## M10 — Deployment Platform
- one-click deploy
- immutable releases
- rollback
- environment configuration
- domain/URL management
- deployment logs and health

## M11 — Knowledge + Skills + Integrations
- project/workspace knowledge
- reusable skills/instructions
- RAG
- MCP server/client integration
- external API connectors
- templates/design systems

## M12 — Collaboration + Enterprise
- multi-user collaboration
- owner/admin/developer/viewer roles
- comments
- SSO-ready architecture
- governance/audit controls
- quotas, model routing and cost reporting
- admin console

## Definition of Done
A milestone is complete only when its code, tests, documentation, CI checks and deployment/runtime verification are green. `main` remains releasable throughout development.
