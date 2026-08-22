# ForgePilot AI — Product Roadmap

ForgePilot is an internal AI app builder. The product goal is a creator-first journey: **Dashboard → Prompt → Plan/Build → Working App Preview → Iterate → Visual Edit → Backend/Connectors → GitHub → Publish → Govern → Operate**.

ForgePilot independently implements comparable workflows and capabilities. It does not copy third-party proprietary source code, private prompts, branding, logos, or protected assets.

## Product rules
- Generated application + AI conversation remain the primary experience.
- A visible control is never complete unless the underlying action works.
- Plan mode proposes without changing code; Build mode changes and verifies.
- Every generated/manual/visual change is recoverable.
- `main` remains releasable and CI/deployment health stays green.
- Enterprise compliance claims are evidence-driven; controls can be implemented, certifications cannot be claimed until independently achieved.
- Competitive capability reviews are used to find workflow gaps, never to copy third-party branding or proprietary implementation.

## P0–P30 — Foundation through enterprise acceptance
P0–P30 established the runtime, creator dashboard, prompt/build workflow, preview, visual editing, code/history, backend, GitHub, connectors, publishing, collaboration, security, knowledge/agents, enterprise governance, identity, deployment, reliability and final acceptance foundations. These capabilities remain regression requirements while P51–P60 productize the creator experience.

## P51 — ForgePilot Identity + Creator UX Reset
**Goal:** ForgePilot must look and feel like its own polished product.
- replace temporary `F` tiles with a distinctive original ForgePilot mark and favicon
- one consistent brand system across login, dashboard, builder, loading/error/empty states and published surfaces
- normalize typography, spacing, icons, buttons, cards, dialogs and side panels
- redesign Connectors into polished provider cards with icon, description, auth type, status and Connect/Manage action
- eliminate browser-default controls, overlapping text, raw forms and placeholder-looking UI
- responsive desktop/tablet/mobile shell
- keyboard/focus/accessibility pass
- screenshot-based visual regression for critical surfaces
- no dead visible control

## P52 — Prompt-to-App Experience 2.0
**Goal:** make idea → working app exceptionally simple.
- richer starter gallery by product category
- prompt enhancement and requirement clarification without blocking simple builds
- screenshot/image/document context ingestion
- intelligent app-name and stack inference
- explicit Plan vs Build behavior and clear execution status
- streamed agent timeline with meaningful steps rather than raw technical noise
- cancel/retry/recover actions
- first-use onboarding and contextual examples
- persist drafts and recent prompts
- useful failure recovery instead of generic errors

## P53 — Live Preview + Visual Design Studio
**Goal:** let users polish an app without manually editing code.
- instant preview refresh and runtime health state
- select any rendered element from preview
- edit text, spacing, size, alignment, colors, typography, borders and layout visually
- desktop/tablet/mobile responsive controls
- reusable themes and design tokens
- workspace brand themes
- image generation/upload/replace flow
- AI edit selected element/component
- undo/redo and visual change history
- accessibility/design-quality checks before publish

## P54 — Full-stack Cloud Runtime
**Goal:** generated projects become real applications, not UI mockups.
- per-project PostgreSQL lifecycle
- schema/table/data browser and safe migrations
- generated CRUD/API contracts
- authentication and application RBAC
- storage/uploads
- realtime events
- background/server functions and jobs
- secrets management
- backend logs and diagnostics
- dev/stage/prod environment model
- backup/restore and migration verification

## P55 — GitHub + Code Ownership 2.0
**Goal:** users always own and can continue their code outside ForgePilot.
- create/connect repository from project
- reliable two-way sync
- branch awareness
- commits with meaningful AI-generated summaries
- visual diff before risky changes
- conflict detection and guided resolution
- version snapshots tied to prompts/builds
- rollback/revert/cherry-pick style recovery
- repository health and sync diagnostics
- CI status inside ForgePilot
- portable export with no ForgePilot runtime lock-in

## P56 — Connector & MCP Ecosystem
**Goal:** connect real business systems cleanly and safely.
- searchable connector marketplace/catalog
- GitHub, Supabase/Postgres, Stripe, Resend/email, Slack and generic REST/webhook first-class connectors
- OAuth, API key and secret-based setup wizards
- connection test, health, reconnect and revoke
- project/workspace/user connection scopes
- permissions and connector allowlists
- custom REST/GraphQL connector builder
- MCP client for external tools/context
- MCP server generation for published ForgePilot apps
- audit every connector action and protect credentials server-side

## P57 — Autonomous Engineering Agent
**Goal:** evolve from code generation into a bounded AI engineering loop.
- planner → implementer → verifier → repair loop
- specialized frontend, backend, database, security and deployment workers
- automatically run compile, unit, integration and browser checks
- inspect failures and repair within bounded retries
- checkpoints and resumable long-running tasks
- background/queued builds
- human approval for destructive/high-risk actions
- execution trace showing files, tools, tests and decisions at a useful level
- model routing/fallback and token/runtime budgets
- deterministic cancellation and recovery

## P58 — Publish, Domains + Production Operations
**Goal:** move from preview to production confidently.
- one-action publish from a verified build
- immutable release artifacts
- custom domain and DNS verification
- automatic TLS
- environment variables and secret bindings
- release history
- rollback/unpublish
- deployment/build/runtime logs
- health/readiness monitoring
- SEO/metadata controls
- security and quality publish gate
- usage/performance telemetry and basic alerting

## P59 — Collaboration, Teams + Enterprise Governance
**Goal:** make ForgePilot usable by real product and engineering teams.
- workspace/project members and invitations
- Owner/Admin/Editor/Viewer/Approver/Publisher permissions
- comments, mentions and preview annotations
- activity feed and notifications
- project folders and ownership transfer
- publish approvals
- enterprise SSO/OIDC/SAML and SCIM completion
- centralized audit log
- connector/model/publish policies
- workspace usage and cost controls
- security center and application inventory
- retention/export/governance controls

## P60 — Competitive Acceptance + ForgePilot GA
**Goal:** prove ForgePilot's complete creator journey with evidence.
- maintain a current capability matrix against leading AI app builders, including Lovable-style workflows, without copying proprietary implementation
- run full E2E: sign in → create → plan/build → preview → visual edit → backend → connector → GitHub → publish → rollback
- zero raw/browser-default production UI on core journeys
- zero non-functional visible controls
- regression suite for authentication, project isolation, GitHub, connectors and publishing
- security, secret leakage and tenant-boundary tests
- accessibility/responsive acceptance
- performance/load/resilience checks
- backup/restore/deployment rollback evidence
- README, architecture, API and operations documentation alignment
- GA checklist with PASS/PARTIAL/GAP evidence; no unsupported parity or certification claims

## P51–P60 execution order
1. **P51 first:** product identity and UX quality become a release gate.
2. **P52–P53:** perfect the creator loop and visual iteration experience.
3. **P54–P56:** make generated apps genuinely full-stack, portable and connected.
4. **P57:** deepen autonomous engineering only after deterministic build/test primitives are reliable.
5. **P58–P59:** production publishing, collaboration and governance.
6. **P60:** final evidence-based GA acceptance and competitive capability review.

## Definition of Done
ForgePilot is complete for this roadmap only when the creator journey works end-to-end: a user can sign in, describe an application, plan/build it, see a working preview, visually refine it, create and inspect backend behavior, connect real services, own/sync the code in GitHub, test and repair it, publish it safely, collaborate with a team and recover from failures. UI presence alone never counts as implementation.