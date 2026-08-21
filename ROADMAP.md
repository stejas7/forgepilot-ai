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
Java 21 / Spring Boot, React/TypeScript, OpenAI integration, Docker/GHCR, GitHub Actions and AWS EC2 runtime.

## P1 — Creator Dashboard — DONE
Creator-first dashboard, workspace navigation, projects, search, resources/connectors entry points, Build/Plan prompt, attachments, templates/gallery and project cards.

## P2 — Prompt → Project → AI Conversation — DONE
Persistent projects/chat, Plan approval, Build changes, streaming status, follow-up prompts, attachments, retry/cancel and usage accounting.

## P3 — Builder + Working Preview
Preview-first builder, generated-app preview/runtime verification, logs/restart, responsive viewport modes, error overlay and repair loop.

## P4 — Visual Editing
DOM selection, text/style/layout editing, responsive overrides, targeted AI prompt, undo/redo and visual-save snapshots.

## P5 — Code + Version History
Monaco, file CRUD/search, snapshots, diff, preview-old-version, restore/revert and recoverable history.

## P6 — Full-stack Backend — IN PROGRESS
Managed PostgreSQL, project isolation, schema/data browser, auth/RBAC contracts, secrets, storage/realtime/API/runtime hardening.

## P7 — GitHub Ownership + Two-way Sync — IN PROGRESS
Connect/create repo, push/pull, branch awareness, conflict protection and sync status.

## P8 — Connectors + Integrations — IN PROGRESS
Connector catalog/framework, project and per-user connections, secret-safe configuration, health status, allowlists and provider integrations.

## P9 — Publish + Share — CORE IMPLEMENTED
Immutable release snapshots, approvals, publish URL, rollback/unpublish, visibility and audit. Custom DNS/TLS automation remains to be hardened.

## P10 — Collaboration + Workspace — CORE IMPLEMENTED
Members, sharing, Owner/Admin/Editor/Viewer/Approver/Publisher roles, comments, activity/audit and workspace governance contracts. Realtime presence/concurrent editing remains.

## P11 — Security + Quality — CORE IMPLEMENTED
Basic/deep scans, secret/SAST/database/accessibility checks, security history, Fix-with-AI handoff, Trust summary, workspace Security Center metrics and publish security gate. External scanners and full E2E automation remain hardening work.

## P12 — Knowledge, Templates + Advanced Agent — CORE IMPLEMENTED
- durable workspace/project knowledge
- retrieval context
- reusable instructions/skills
- templates/remix foundation
- queued agent tasks and lifecycle
- context bundle for agent execution
- next hardening: full RAG embeddings/vector retrieval, autonomous multi-agent execution, MCP client/server and external tool execution

## P13 — Enterprise Governance + Administration — CORE IMPLEMENTED
- enterprise admin read model
- workspace-wide app inventory
- Workspace Insights metrics
- governance settings
- SSO/SAML/OIDC and SCIM configuration contracts
- workspace RBAC/publishing/connector/model policies
- audit access
- security posture and abandoned-project visibility
- next hardening: live IdP/SCIM protocol execution, richer admin UI, export/retention workflows and scheduled governance jobs

## P14 — Lovable-style Parity Audit — ACTIVE
- explicit capability matrix across Dashboard, AI, Preview, Visual Edit, Code/Versions, Backend, GitHub, Connectors, Publish, Collaboration, Security, Knowledge/Agent and Enterprise
- statuses are PASS / PARTIAL / GAP
- remaining gaps are intentionally visible and block a false "parity complete" claim
- final parity complete only when PARTIAL = 0 and GAP = 0

## P15 — Production Hardening & GA Readiness — ACTIVE
- single production-readiness endpoint
- parity gate
- security-coverage gate
- security-blocker gate
- durable project-state check
- CI/build/deployment/EC2 health must be green before GA
- regression suite across creator journey
- performance/resource review
- backup/restore and disaster-recovery verification
- secret/config validation
- observability, alerts and operational runbook
- release checklist and rollback verification

## Immediate implementation sequence
1. Keep P3–P8 hardening/regression work green.
2. P9–P11 core implemented; close production-scale gaps as found by regression.
3. P12 core implemented; deepen RAG/agents/MCP.
4. P13 core implemented; deepen SSO/SCIM/admin execution.
5. Run P14 continuously and close every PARTIAL/GAP.
6. P15 is the final release gate; ForgePilot is complete only when `/api/readiness` reports READY_FOR_GA and CI/deployment are green.

## Definition of Done
ForgePilot is complete only when the creator journey works end-to-end: a user can enter from the dashboard, describe an app, plan or build it, see a real working preview, iterate conversationally and visually, add backend/integrations, recover versions, own/sync code through GitHub, publish a controlled live snapshot, collaborate, and operate under enterprise identity, governance, security and audit controls. UI placeholders do not count as completed functionality. P14 must show no remaining PARTIAL/GAP capabilities and P15 must pass before the product is called complete.
