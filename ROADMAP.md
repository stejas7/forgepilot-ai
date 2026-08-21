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
- Enterprise compliance claims are evidence-driven: controls may be implemented in product, but certifications such as SOC 2 or ISO 27001 are never claimed unless independently achieved.

## P0 — Runtime Foundation — DONE
- Java 21 / Spring Boot platform API
- React/TypeScript web shell
- OpenAI server-side integration
- Docker/GHCR
- GitHub Actions CI/CD
- AWS EC2 runtime and health verification

## P1 — Lovable-style Dashboard — DONE
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

## P2 — Prompt → Project → AI Conversation — DONE
- submitting dashboard prompt creates/opens a project
- persistent projects and workspace state
- persistent chat sessions/messages
- Plan mode with editable structured plan and approval
- Build/Agent mode with real workspace changes and recoverable snapshots
- streaming execution/status events wired into the creator UI
- follow-up prompts modify the existing project
- dashboard and builder attachments with text/image AI context
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

## P6 — Full-stack Backend — IN PROGRESS
- managed PostgreSQL-backed generated applications
- per-project database isolation
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

## P7 — GitHub Ownership + Two-way Sync — IN PROGRESS
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

## P8 — Connectors + Integrations — IN PROGRESS
- Connectors catalog from dashboard and builder
- OAuth/API-key connector framework
- project/app connectors
- context/chat connectors
- app-user/per-user connectors with source-system permissions preserved
- centrally configured workspace connectors
- connector allowlists and role-based connector access
- Stripe/payments
- email provider integration
- Jira / Confluence / Notion / Linear style context connectors
- n8n / Miro style workflow/design connectors
- CRM / database / enterprise-system connectors
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
- enterprise publishing approval policy
- separate edit / approve / publish permissions
- environment-aware publishing controls

## P10 — Collaboration + Workspace
- workspace members
- project sharing/access
- owner/admin/editor/viewer/approver/publisher roles
- invitations/access requests
- comments/annotations
- activity/audit history
- folders
- notifications/inbox
- concurrent editing foundation
- workspace settings
- workspace-level brand/design-system consistency
- reusable company authentication profile for generated apps

## P11 — Security + Quality
- browser/E2E verification of generated apps
- unit/integration test generation
- dependency vulnerability scanning
- secret scanning
- SAST/security agent
- auth/database policy review
- database/RLS/cloud configuration checks
- accessibility checks
- automatic basic security scan before publish
- on-demand deep security scan
- recurring deep scans for enterprise workspaces
- optional safe auto-fix for non-breaking findings
- policy gate that can block publish on critical findings
- security history
- generated-app Trust Center/security summary
- WAF/rate-limit/network-isolation readiness controls
- abuse/risk monitoring hooks
- external security-provider integration surface such as Wiz

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

## P13 — Enterprise Governance + Administration
- enterprise admin console
- workspace-wide application inventory
- Workspace Insights dashboard
- total-project / externally-published / review-priority metrics
- filters for PII projects, abandoned apps, open findings and missing owners
- SSO with SAML and OIDC providers
- Okta / Azure AD / Google-ready identity provider configuration
- SCIM provisioning/deprovisioning
- server-enforced workspace RBAC
- centralized role/policy management
- project visibility policy and private-by-default options
- editing / approval / publishing permission separation
- centralized workspace connector policy
- connector/model allowlists
- organization policies
- centralized secrets policy
- immutable/append-only audit events
- audit search/export
- audit retention controls
- publishing policies and approval gates
- workspace-level brand consistency and design-system policy
- reusable workspace authentication configuration
- usage quotas/budgets
- model routing and cost reporting
- abandoned-project cleanup and ownership reassignment workflow
- company-wide Security Center
- scheduled security scans across projects
- governance view of generated databases, published endpoints and sensitive-data exposure
- legal/compliance resource surfaces for DPA, subprocessors, security docs and change log
- enterprise support/onboarding resource area and escalation metadata
- no certification badges unless independently achieved

## P14 — Final Lovable-style Parity Audit
- compare Dashboard workflow capability-by-capability
- compare project creation flow
- compare Plan vs Build/Agent behavior
- compare generated-app preview/iteration
- compare Visual Editing
- compare code/version history
- compare backend/auth/storage
- compare GitHub sync
- compare connectors and app-user connectors
- compare Publish/share/domain and approval flow
- compare collaboration/workspace controls
- compare enterprise dashboard, SSO/SCIM/RBAC, audit, connector governance and Workspace Insights
- compare security scan/publish-gate/Trust Center workflows
- remove dead controls and developer-console-first UX
- responsive and performance review
- security review

## Immediate implementation sequence

1. P1 creator dashboard — complete.
2. P2 project creation + persistent AI conversation — complete.
3. Complete P3/P4 regression hardening.
4. Complete P5 Code + Version History.
5. **NOW: finish P6 PostgreSQL/full-stack backend, P7 GitHub conflict-aware sync, and P8 connector framework.**
6. Implement P9 publishing/approval controls.
7. Implement P10 collaboration/workspace roles.
8. Implement P11 security scans, publish gates and Trust Center.
9. Implement P12 knowledge/advanced agent.
10. Implement the full P13 enterprise governance suite described above.
11. Run P14 parity audit; remaining gaps must be explicitly recorded and closed.

## Definition of Done

ForgePilot is complete only when the creator journey works end-to-end: a user can enter from the dashboard, describe an app, plan or build it, see a real working preview, iterate conversationally and visually, add backend/integrations, recover versions, own/sync code through GitHub, publish a controlled live snapshot, collaborate, and operate under enterprise identity, governance, security and audit controls. UI placeholders do not count as completed functionality.
