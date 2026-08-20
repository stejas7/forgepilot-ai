# ForgePilot AI — Full Product Roadmap

ForgePilot independently targets the complete internal AI application-builder workflow represented by modern products such as Lovable. We reproduce capabilities and workflows, not proprietary source code, prompts, branding or protected assets.

## Delivery contract

- Implement milestones sequentially on `main`.
- Keep `main` releasable and EC2 deployable.
- A UI placeholder is **not** considered implemented.
- Every feature must have a real backend/runtime path where applicable.
- Each milestone ends with CI green + EC2 runtime verification.
- Preserve existing working functionality while adding the next milestone.

## M0 — Green Foundation — IMPLEMENTED
- Java 21 / Spring Boot API
- React/TypeScript builder shell
- Docker production image
- GitHub Actions CI
- GHCR image publishing
- AWS EC2 deployment
- health/runtime verification
- OpenAI server-side integration

## M1 — Projects + Persistent Workspace — IN PROGRESS
- project CRUD
- persistent project/workspace model
- project settings and visibility
- workspace file persistence
- audit events
- PostgreSQL-backed platform persistence
- remove remaining in-memory production state

## M2 — AI Conversation / Plan / Build
- persistent chat sessions and messages
- Plan mode
- Build mode
- structured plans
- streaming generation events
- model selection/routing
- prompt/context management
- follow-up change requests
- cancellation/retry
- usage/token accounting

## M3 — Real Code Workspace
- generated filesystem abstraction
- file tree
- file search
- read/write/create/delete/rename files
- Monaco editor
- syntax highlighting
- save edited files
- dirty-state tracking
- targeted file/code context
- multi-file selection
- attachments
- image/screenshot input
- import document/context files

## M4 — Sandbox + True Live Preview
- isolated per-project execution sandbox
- dependency installation
- build/run commands
- runtime logs
- generated application process lifecycle
- real preview URL/proxy
- refresh/restart
- desktop/tablet/mobile viewport modes
- open preview in new tab
- error overlay
- resource/network/time limits

## M5 — Agentic Generation + Automatic Repair
- orchestrator
- frontend agent
- backend agent
- database agent
- test agent
- reviewer agent
- parallel subagents
- tool execution
- compile/build validation
- runtime validation
- automatic error diagnosis
- repair loop
- user-visible execution timeline

## M6 — Version History + GitHub Sync
- automatic snapshots
- named versions
- file-level diff
- version diff
- restore/revert
- GitHub OAuth/App connection
- create/connect repository
- commit
- branch
- push
- pull
- conflict detection
- conflict resolution
- two-way synchronization
- GitHub status in builder

## M7 — Generated Backend Platform
- PostgreSQL provisioning
- schema generation
- migrations
- data browser/table editor
- generated REST APIs
- server functions
- authentication
- email/password login
- social/OAuth login
- session management
- generated RBAC
- row-level access policies
- object/file storage
- realtime subscriptions
- secrets/environment variables
- backend logs

## M8 — Visual / Design Builder
- preview DOM/component bridge
- click-to-select element
- component hierarchy
- visual property inspector
- text editing
- typography
- colors
- spacing
- sizing
- borders/radius/shadows
- flex/grid/layout
- visibility
- responsive overrides
- drag/reorder where safe
- comments/annotations
- undo/redo
- design-system tokens
- reusable components
- screenshot-to-design context
- Figma/design import workflow

## M9 — Integrations + Payments
- integration catalog
- API-key based connectors
- OAuth connectors
- Stripe/payment integration
- email provider integration
- storage/provider integrations
- analytics integration
- webhook configuration
- external REST/GraphQL APIs
- custom connector definitions
- connector secrets
- integration health/status

## M10 — Validation + Security
- unit test generation/execution
- integration tests
- browser/E2E tests
- accessibility checks
- dependency vulnerability scanning
- secret scanning
- SAST/security agent
- auth/RBAC review
- database policy review
- deployment readiness report
- automatic remediation suggestions
- policy gates
- security history
- Trust Center style security summary for published apps

## M11 — Publishing + Domains
- one-click Publish
- immutable releases
- deployment progress
- deployment logs
- release history
- rollback
- preview/staging/production environments
- environment variables per environment
- generated public URL
- custom domains
- DNS verification
- TLS/SSL
- SPA routing
- SEO metadata
- favicon/social metadata
- health monitoring
- publish/unpublish

## M12 — Knowledge + Skills + RAG
- project knowledge
- workspace/org knowledge
- persistent instructions
- reusable skills
- reusable prompt rules
- RAG ingestion
- document indexing
- semantic retrieval
- knowledge source management
- context visibility/debugging
- templates
- starter applications
- reusable design systems

## M13 — Collaboration
- users/members
- invite flow
- project sharing
- owner/admin/developer/viewer roles
- permissions
- comments
- annotations
- activity feed
- audit history
- concurrent editing foundation
- team/workspace organization

## M14 — Enterprise Governance
- SSO/SAML-ready architecture
- SCIM-ready provisioning
- organization policies
- centralized secrets policies
- model allowlists
- connector allowlists
- audit export
- retention controls
- usage quotas
- budgets
- model routing
- cost reporting
- admin console

## M15 — MCP + Agent Ecosystem
- MCP client support
- MCP server for ForgePilot tooling
- expose published application capabilities through MCP where configured
- external agent integrations
- scoped tool permissions
- agent credentials
- tool discovery
- invocation audit logs

## M16 — Product Polish / Parity Review
- onboarding
- templates/gallery
- keyboard shortcuts
- command palette
- responsive builder UI
- loading/error/empty states
- notifications/toasts
- account/settings UX
- usage dashboard
- billing/credit abstraction for internal quotas
- documentation/help surfaces
- full capability-by-capability parity audit
- remove remaining placeholders/dead controls
- performance/load review
- security review

## Immediate sequential implementation queue

1. Finish M1 persistent workspace.
2. Complete M2 persistent AI conversation and follow-up edits.
3. Complete M3 editable code workspace.
4. Build M4 true executable live preview.
5. Add M5 validation/repair agents.
6. Complete M6 GitHub synchronization and version diff.
7. Implement M7 real backend/auth/storage platform.
8. Implement M8 visual editor.
9. Implement M9 integrations/payments.
10. Implement M10 validation/security.
11. Implement M11 full publishing/domain platform.
12. Implement M12 knowledge/skills/RAG.
13. Implement M13 collaboration.
14. Implement M14 enterprise governance.
15. Implement M15 MCP/agent ecosystem.
16. Finish M16 parity/polish audit.

## Definition of Done

A milestone is complete only when functionality is real (not merely visible), relevant tests/build checks pass, documentation is updated, CI is green, and the deployed EC2 runtime is verified. The final parity review must explicitly compare every supported workflow against the reference capability matrix and identify any remaining gap before ForgePilot is declared complete.
