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
- P14 parity remains a continuous audit rather than a one-time checkbox.

## P0 — Runtime Foundation — DONE
Java 21/Spring Boot, React/TypeScript, OpenAI integration, Docker/GHCR, GitHub Actions, AWS EC2 runtime.

## P1 — Creator Dashboard — DONE
Creator dashboard, workspace navigation, projects/search/resources, Build/Plan composer, attachments, templates/gallery.

## P2 — Prompt → Project → AI Conversation — DONE
Persistent projects/chat, Plan approval, Build changes, streaming status, follow-ups, attachments, retry/cancel, usage accounting.

## P3 — Builder + Working Preview — HARDENING
Preview-first builder, generated preview/runtime verification, logs/restart, responsive viewports, error overlay, repair loop; production sandbox/dependency execution remains hardening.

## P4 — Visual Editing — HARDENING
DOM selection, stable element IDs, text/style/layout editing, responsive overrides, selected-element AI, undo/redo, snapshots; deepen component/source mapping and design tokens.

## P5 — Code + Version History — CORE IMPLEMENTED
Monaco, file CRUD/search, dirty/save state, snapshots, timeline, diff, historical preview, restore/revert; deepen named versions and prompt replay.

## P6 — Full-stack Backend — IN PROGRESS
Managed PostgreSQL, per-project isolation, migrations/schema/data browser, generated APIs, auth/RBAC/session runtime, secrets, storage/uploads, realtime, functions and backend logs.

## P7 — GitHub Ownership + Two-way Sync — IN PROGRESS
Connect/create repo, push/pull, branch awareness, sync baseline/conflict protection, status, reconnect; deepen conflict-resolution UX and provider auth.

## P8 — Connectors + Integrations — IN PROGRESS
Connector catalog/framework, project/app/user connectors, OAuth/API-key/webhook/REST/GraphQL, workspace allowlists, permissions, health, Stripe/email/context/workflow/enterprise integrations.

## P9 — Publish + Share — CORE IMPLEMENTED
Immutable releases, approvals, publish URL, visibility, update/republish, history, rollback/unpublish and audit; finish custom domains, DNS verification, TLS, metadata/SEO and deployment observability.

## P10 — Collaboration + Workspace — CORE IMPLEMENTED
Members, sharing, Owner/Admin/Editor/Viewer/Approver/Publisher roles, comments, activity/audit, workspace governance; finish access requests, notifications, folders, presence and concurrent editing.

## P11 — Security + Quality — CORE IMPLEMENTED
Basic/deep scans, secret/SAST/database/accessibility checks, history, Fix-with-AI, Trust summary, workspace Security Center and publish gate; deepen dependency scanners, E2E, scheduled scans, external security providers and network controls.

## P12 — Knowledge, Templates + Advanced Agent — CORE IMPLEMENTED
Durable knowledge, retrieval context, skills/instructions, templates/remix foundation and queued agent tasks. Finish embeddings/vector RAG, autonomous multi-agent execution, MCP client/server and external tools.

## P13 — Enterprise Governance + Administration — CORE IMPLEMENTED
Enterprise admin read model, app inventory, Workspace Insights, governance settings, SSO/SAML/OIDC + SCIM contracts, RBAC/publish/connector/model policies, audit/security posture. Finish live IdP/SCIM execution, exports, retention and scheduled governance.

## P14 — Lovable-style Capability Parity Audit — CONTINUOUS
Capability matrix across dashboard, creation, Plan/Build, preview, visual edit, code/history, backend, GitHub, connectors, publishing, collaboration, security, knowledge/agents and enterprise. PASS/PARTIAL/GAP only; no false parity claim.

## P15 — Production Hardening & GA Readiness — ACTIVE
Readiness endpoint, parity/security/state gates, regression suite, performance/resource review, backup/restore/DR, config validation, observability, alerts, operational runbook, release/rollback checklist.

## P16 — Enterprise Identity Runtime
- real SAML 2.0 and OIDC login flows
- Okta, Microsoft Entra ID and Google Workspace configuration
- domain discovery and enforced SSO
- SCIM users/groups provisioning and deprovisioning
- group → ForgePilot role mapping
- session/token lifecycle and emergency admin access
- reusable enterprise auth profiles for generated applications
- identity/audit diagnostics

## P17 — Advanced Workspace Governance
- organization/workspace hierarchy
- private-by-default/project-visibility policies
- granular server-enforced permissions
- connector/model/provider allowlists
- environment and publishing policies
- centralized secrets policy
- project ownership reassignment
- abandoned-app review/cleanup workflow
- policy simulation before enforcement
- immutable policy-change audit

## P18 — Workspace Insights & Application Inventory
- enterprise landing/Insights dashboard
- total apps, active apps and externally published apps
- apps requiring review
- PII/sensitive-data indicators
- missing-owner and abandoned-app indicators
- open security findings
- backend/database exposure inventory
- connector/provider usage inventory
- filtering/search/export and drill-down
- executive/security/engineering views

## P19 — Enterprise Security Center
- organization-wide security posture
- recurring/scheduled deep scans
- dependency/SCA, secret, SAST, auth/RBAC/RLS and cloud checks
- WAF/rate-limit/network-isolation readiness
- abuse/risk monitoring hooks
- external security provider adapters such as Wiz-class tools
- finding assignment, SLA, suppression and evidence
- AI remediation + verification
- publish blocking by workspace policy

## P20 — Audit, Trust & Compliance Operations
- searchable append-only audit log
- export and retention policies
- identity, connector, secret, publishing and permission events
- Trust Center for product/security evidence
- DPA/subprocessor/security-document resource surfaces
- compliance control/evidence mapping
- change log and security advisories
- legal/compliance owner workflows
- no certification badges until independently achieved

## P21 — Enterprise Connector Governance
- centralized connector catalog
- workspace-managed OAuth/API-key connections
- per-user/app-user connectors preserving source permissions
- role/project connector access
- connector approval workflow
- secret rotation/expiry
- connection health and diagnostics
- connector usage/audit inventory
- Jira/Confluence/Notion/Linear/Miro/n8n/CRM/database/provider adapters
- REST/GraphQL/webhook custom connectors

## P22 — Design Systems, Brand Governance & Templates
- workspace design systems
- colors/typography/spacing/component tokens
- approved reusable components
- locked/protected brand primitives
- company templates and starter applications
- remix/clone with policy preservation
- screenshot/design ingestion
- brand-consistency checks
- workspace-level visual policies
- reusable authentication/application shells

## P23 — Advanced Agent Runtime
- autonomous multi-step execution
- planner/executor/verifier loop
- specialized coding, security, database, design and deployment subagents
- queued/background prompts
- resumable jobs and checkpoints
- bounded retry/cancel/timeouts
- human approval gates for risky operations
- MCP client/server
- external agent/tool integrations
- full execution trace and cost accounting

## P24 — Enterprise Data & Backend Platform
- hardened managed PostgreSQL lifecycle
- backups/PITR and restore testing
- storage/object uploads
- realtime subscriptions
- generated server functions/jobs
- schema migration safety
- RLS/data-access policy generator and verifier
- data residency/configuration hooks
- PII discovery/classification
- environment promotion dev/stage/prod
- backend observability and quotas

## P25 — Production Runtime & Deployment Platform
- isolated per-project runtime/container sandbox
- dependency install/build/run for generated frameworks
- CPU/memory/time/network limits
- build cache and artifact lifecycle
- deployment environments
- custom domains + DNS verification
- automated TLS
- immutable deployment artifacts
- canary/rollback foundation
- runtime/build/deployment logs
- autoscaling/readiness architecture

## P26 — Collaboration 2.0
- realtime presence
- concurrent editing foundation
- comments/threads/mentions
- preview/element annotations
- notifications/inbox
- access requests and approval
- folders/collections
- project transfer/ownership
- activity feed
- conflict-safe collaborative editing

## P27 — Developer & Platform Extensibility
- public/internal ForgePilot API
- API tokens/service accounts
- webhooks/event subscriptions
- CLI
- SDKs and generated client contracts
- plugin/tool extension model
- MCP registry/management
- automation hooks
- import/export portability
- admin-safe rate limits and scopes

## P28 — Usage, Cost, Quotas & FinOps
- per-user/project/workspace AI usage
- model/token/tool/runtime cost accounting
- configurable budgets and quotas
- model routing/fallback policy
- usage alerts
- cost center/team attribution
- connector/runtime/storage consumption
- enterprise usage exports
- admin dashboards
- optimization recommendations

## P29 — Enterprise Reliability, Support & Operations
- SLO/SLI dashboards
- centralized metrics/logs/traces
- alerting/on-call hooks
- backup/restore/DR drills
- incident management and status communication hooks
- support/onboarding resource center
- escalation metadata
- tenant diagnostics bundle
- feature flags and controlled rollout
- maintenance and migration tooling

## P30 — Final Enterprise Product Acceptance
- rerun full P14 capability audit
- zero unexplained GAP items for agreed internal parity scope
- explicitly accepted PARTIAL items only where external/legal/certification dependencies exist
- full creator-journey E2E regression
- enterprise identity/SCIM regression
- RBAC/publishing approval regression
- connector permission regression
- GitHub ownership/sync regression
- security/publish-gate regression
- Workspace Insights/audit/Trust Center regression
- performance/load/resilience tests
- backup/restore/rollback test evidence
- accessibility/responsive review
- dead-code/placeholder/control cleanup
- README/architecture/runbook/API documentation alignment
- CI/CD + EC2/production deployment verification
- final acceptance report with PASS/PARTIAL/GAP and evidence links

## Execution sequence
1. Keep P3–P11 regression green while closing their production-scale gaps.
2. Deepen P12/P13 where required by enterprise workflows.
3. Run P14 continuously as the master gap register.
4. P15 remains the near-term production readiness gate.
5. Implement P16–P22 enterprise identity, governance, insights, security, compliance, connectors and brand/design capabilities.
6. Implement P23–P29 advanced agents, backend/runtime, collaboration, extensibility, FinOps and reliability.
7. P30 is final enterprise acceptance; nothing is called complete merely because a UI exists.

## Definition of Done
ForgePilot is complete for the agreed internal enterprise scope only when the creator journey works end-to-end and enterprise controls are enforced server-side: build/plan, real preview, visual editing, backend/integrations, version recovery, GitHub ownership, controlled publishing, collaboration, enterprise identity, Workspace Insights, security, connector governance, audit/Trust operations, production reliability and cost governance. P30 must contain test/evidence for the final acceptance decision.
