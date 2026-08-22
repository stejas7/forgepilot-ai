# ForgePilot Roadmap — P58 to P100

## Delivery contract
Every phase follows the same release flow:

**Plan → Develop → Implement → Commit → CI → Merge to `main` → Deploy from `main` → Smoke-test → Next phase**

Rules:
- Production deploys from `main` only.
- No visible dead controls. A visible action must work end-to-end or be explicitly marked unavailable.
- A phase is complete only after CI, deployment, and smoke validation succeed.
- Security/compliance claims must reflect implemented controls only.
- Lovable.dev is a product-completeness benchmark, not a design or code-copy target.

## P58–P60 — Production creator foundation
### P58 — AI Build Workspace 2.0
- Unified chat/agent, preview, code and backend workspace.
- Reliable iterative prompt execution and follow-up changes.
- Rich execution status, cancel/retry and structured errors.
- Better responsive builder layout and usable preview states.

### P59 — Source Ownership & Delivery Pipeline
- GitHub repository create/connect/sync workflow.
- Branch/commit visibility from ForgePilot.
- CI evidence, build status and deployment history.
- Version history, rollback and immutable release references.

### P60 — ForgePilot GA Acceptance
- End-to-end prompt → plan → build → preview → verify → GitHub → deploy journey.
- Public, login, workspace, project and release UX acceptance.
- No dead navigation or placeholder product controls.
- GA smoke pack and release readiness baseline.

## P61–P70 — Full-stack product creation
### P61 — Visual Editor
- Select generated UI elements in preview and modify text/layout/style from ForgePilot.

### P62 — Component-aware Editing
- Map preview elements to source components and propose safe patches.

### P63 — Backend Generator
- Generate Spring Boot APIs, DTOs, validation, service and persistence layers from plan context.

### P64 — Database Studio
- Schema design, migrations, seeded data, PostgreSQL management and safe evolution.

### P65 — Authentication Builder
- Project-level auth scaffolding, roles, protected routes and session patterns.

### P66 — File & Object Storage
- Upload/download patterns, metadata and generated app integration.

### P67 — Realtime Features
- WebSocket/SSE generation patterns for notifications and live dashboards.

### P68 — Background Jobs
- Scheduled/async job generation, retries, monitoring and dead-letter handling.

### P69 — API Integration Studio
- OpenAPI/REST import, external API mapping and generated typed clients.

### P70 — Full-stack Acceptance
- Generated frontend + backend + database + auth app works from one prompt through deploy.

## P71–P80 — Connectors, agents and engineering intelligence
### P71 — Connector Marketplace
- Searchable connector catalog, categories, status and configuration UX.

### P72 — OAuth Connector Framework
- Reusable OAuth connection model for supported external services.

### P73 — Secret-backed API Connectors
- API key/webhook connectors with masking, validation and scoped storage.

### P74 — MCP Integration Layer
- MCP server registration, capability discovery and governed tool access.

### P75 — Multi-agent Orchestration
- Planner, coder, reviewer, test and security agent roles with controlled handoff.

### P76 — Repository Intelligence
- Codebase indexing, semantic retrieval and architecture-aware context selection.

### P77 — Change Impact Analysis
- Detect impacted files, APIs, tests, data model and runtime dependencies before changes.

### P78 — Autonomous Fix Loop
- Build/test failure → diagnose → propose patch → rerun under bounded policy.

### P79 — Engineering Memory
- Persist project decisions, conventions, accepted plans and release context.

### P80 — Agent Acceptance
- Demonstrate reliable multi-step engineering tasks with evidence and bounded recovery.

## P81–P90 — Enterprise governance and collaboration
### P81 — Team Workspaces
- Workspace creation, members, invitations and project ownership.

### P82 — Enterprise RBAC
- Owner/Admin/Editor/Viewer plus deploy/review/security approval permissions.

### P83 — SAML/OIDC Enterprise SSO
- Enterprise identity provider integration framework.

### P84 — SCIM Provisioning
- User/group provisioning lifecycle for enterprise tenants.

### P85 — Audit & Evidence Center
- Immutable audit events for auth, changes, builds, approvals and deployment.

### P86 — Environment Governance
- Development/staging/production environments with policies and separate secrets.

### P87 — Approval Workflows
- Required review/security/deployment approvals configurable by workspace.

### P88 — Policy Engine
- Rules for models, connectors, dependencies, repositories, environments and release gates.

### P89 — Usage & Cost Governance
- Workspace/project AI usage, build usage, connector activity and budget controls.

### P90 — Enterprise Acceptance
- Enterprise reference workflow for identity, RBAC, audit, approval and controlled deployment.

## P91–P100 — Reliability, scale and platform maturity
### P91 — Observability
- Central logs, metrics, traces, AI execution diagnostics and deploy health.

### P92 — Reliability Engineering
- Timeouts, retries, circuit breakers, idempotency and resilient agent execution.

### P93 — Performance Engineering
- Frontend performance, API latency, DB query and build pipeline optimization.

### P94 — Horizontal Scale
- Stateless service design, scalable workers and durable queues for generation workloads.

### P95 — Multi-tenant Isolation
- Strong workspace/project isolation across data, secrets, jobs and generated assets.

### P96 — Disaster Recovery
- Backup/restore procedures, release recovery and environment reconstruction.

### P97 — Security Hardening
- Dependency/SAST/secret/container checks, rate limits, headers and attack-surface reduction.

### P98 — Quality Engineering Platform
- Unit/integration/e2e/contract/security/performance test orchestration with evidence.

### P99 — Release & Upgrade Discipline
- Versioned migrations, compatibility checks, rollback plans and controlled platform upgrades.

### P100 — ForgePilot Platform 2.0 Acceptance
- Complete public-to-production creator journey.
- Stable end-to-end full-stack generation.
- Enterprise governance baseline.
- Production observability/reliability/security acceptance.
- No critical dead controls, broken navigation, identity leakage or placeholder workflows.
- Release candidate passes automated and production smoke suites.

## Phase completion checklist
For every P58–P100 phase:
1. Scope and acceptance criteria documented.
2. Feature branch created from current `main`.
3. Implementation completed with relevant tests.
4. Changes committed with phase-specific commit message.
5. CI green.
6. PR merged into `main`.
7. Single `main` pipeline deploys production.
8. Health + functional smoke checks pass.
9. Only then start the next phase.
