# ForgePilot AI — Product Roadmap

ForgePilot is an internal AI app builder. The product goal is a simple creator-first journey: **Dashboard → Prompt → Plan/Build → Working App Preview → Iterate → Visual Edit → Backend/Connectors → GitHub → Publish**.

ForgePilot independently implements comparable workflows and capabilities. It does not copy third-party proprietary source code, private prompts, branding, logos, or protected assets.

## Product rules

- The generated application and AI conversation are the primary experience; engineering machinery stays secondary.
- A visible button/tab is never considered implemented unless the underlying action works.
- Plan mode reasons and proposes without changing code.
- Build/Agent mode changes the project and verifies the result.
- Every generated change creates recoverable history.
- `main` stays releasable; CI and EC2 health must remain green.

## P0 — Runtime Foundation — DONE
- Java 21 / Spring Boot platform API
- React/TypeScript web shell
- OpenAI server-side integration
- Docker/GHCR
- GitHub Actions CI/CD
- AWS EC2 runtime and health verification

## P1 — Lovable-style Dashboard — NOW
- creator-first dashboard replaces developer-console landing page
- workspace selector and collapsible sidebar
- Dashboard, Search, Resources, Connectors
- All projects, Starred, Created by me, Shared with me
- Recents
- central "What do you want to build?" prompt
- Build / Plan selector
- attachment/image/screenshot entry point
- connector/design/template entry points
- templates/resources gallery
- project cards and search/filter/sort
- command palette / keyboard shortcuts
- account/settings/inbox surfaces

## P2 — Prompt → Project → AI Conversation
- submitting dashboard prompt creates/opens a project
- persistent projects and workspace state
- persistent chat sessions/messages
- Plan mode with editable structured plan and approval
- Build/Agent mode with real code changes
- streaming execution/status events
- follow-up prompts modify the existing project
- attachments and image context
- cancel/retry
- usage accounting

## P3 — Builder Experience + Working Preview
- project builder centered on the generated application
- AI conversation panel
- true generated-app preview, not a mock preview card
- isolated project runtime/sandbox
- dependency install/build/run
- runtime/build logs
- preview refresh/restart
- desktop/tablet/mobile preview
- open preview separately
- page navigation
- error overlay
- automatic compile/runtime verification
- AI repair loop

## P4 — Visual Editing
- click/select elements directly in preview
- stable element/component targeting
- text editing
- color/typography/spacing/layout/style controls
- responsive overrides
- targeted prompt against selected component
- source-code changes generated from visual edits
- instant preview refresh
- undo/redo
- reusable design tokens/components
- screenshot/design context

## P5 — Code + Version History
- secondary Code view
- real file tree/search
- Monaco editor
- create/read/update/delete/rename files
- save and dirty-state handling
- targeted file context for AI
- automatic snapshots for AI/manual/visual changes
- history timeline
- bookmarks/named versions
- file/version diff
- preview older version
- restore/revert
- edit/replay earlier prompt from a version

## P6 — Full-stack Backend
- managed PostgreSQL-backed generated applications
- schema generation and migrations
- data/table browser
- authentication
- email/password and supported OAuth flows
- sessions
- generated RBAC / data-access policies
- storage/uploads
- realtime capabilities
- generated APIs/server functions
- project secrets/environment variables
- backend/runtime logs
- backend changes driven from the same AI conversation

## P7 — GitHub Ownership + Two-way Sync
- connect GitHub account/repository
- create or connect repo
- export complete generated code
- commits from ForgePilot changes
- default-branch two-way synchronization
- pull external GitHub edits back into ForgePilot
- conflict detection/resolution
- branch awareness
- sync/status UI
- disconnect/reconnect safely

## P8 — Connectors + Integrations
- Connectors catalog from dashboard and builder
- OAuth/API-key connector framework
- project/app connectors
- context/chat connectors
- Stripe/payments
- email provider integration
- external REST/GraphQL APIs
- webhooks
- analytics/storage/provider integrations
- connector secrets and permissions
- connection health/status

## P9 — Publish + Share
- one-click Publish from builder top bar
- publish immutable current snapshot
- generated shareable app URL
- Update/republish after later edits
- unpublish
- project/editor access separate from published-app access
- internal/workspace vs public access
- custom domains
- DNS verification
- TLS/SSL
- site title/favicon/description/social image
- SEO/accessibility review
- deployment progress/logs/history
- rollback

## P10 — Collaboration + Workspace
- workspace members
- project sharing/access
- owner/admin/editor/viewer-style roles
- invitations/access requests
- comments/annotations
- activity/audit history
- folders
- notifications/inbox
- concurrent editing foundation
- workspace settings

## P11 — Security + Quality
- browser/E2E verification of generated apps
- unit/integration test generation
- dependency vulnerability scanning
- secret scanning
- SAST/security agent
- auth/database policy review
- accessibility checks
- publish security gate/report
- remediation through AI conversation
- security history/trust summary

## P12 — Knowledge, Templates + Advanced Agent
- templates/gallery/remix workflow
- design systems
- project/workspace knowledge
- reusable instructions/skills
- RAG ingestion/retrieval
- autonomous multi-step agent execution
- queued prompts
- specialized subagents
- MCP client/server capabilities
- external agent/tool integrations

## P13 — Enterprise + Administration
- SSO/SAML-ready architecture
- SCIM-ready provisioning
- organization policies
- audit export/retention controls
- connector/model allowlists
- usage quotas/budgets
- model routing and cost reporting
- admin console
- internal publishing policies

## P14 — Final Lovable-style Parity Audit
- compare Dashboard workflow capability-by-capability
- compare project creation flow
- compare Plan vs Build/Agent behavior
- compare generated-app preview/iteration
- compare Visual Editing
- compare code/version history
- compare backend/auth/storage
- compare GitHub sync
- compare connectors
- compare Publish/share/domain flow
- compare collaboration/workspace controls
- remove dead controls and developer-console-first UX
- responsive and performance review
- security review

## Immediate implementation sequence

1. Rebuild the current frontend shell as **P1 creator dashboard**.
2. Wire dashboard prompt directly into **P2 project creation + persistent AI conversation**.
3. Make the project screen **P3 preview-first builder**.
4. Add **P4 Visual Editing** before expanding developer-oriented panels.
5. Complete **P5 Code + Version History** as secondary power-user tooling.
6. Implement **P6 full-stack backend**, then **P7 GitHub**, **P8 Connectors**, and **P9 Publish**.
7. Complete collaboration, security, knowledge/agent and enterprise layers.
8. Run P14 parity audit; remaining gaps must be explicitly recorded and closed.

## Definition of Done

ForgePilot is complete only when the creator journey works end-to-end: a user can enter from the dashboard, describe an app, plan or build it, see a real working preview, iterate conversationally and visually, add backend/integrations, recover versions, own/sync code through GitHub, publish a controlled live snapshot, and collaborate. UI placeholders do not count as completed functionality.
