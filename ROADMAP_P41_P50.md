# ForgePilot P41–P50 Product Hardening Roadmap

P41–P50 extend ForgePilot from platform closure into secure production operation. These milestones preserve the existing P14/P30/P40 evidence model: a control is not complete because a UI or API exists; it must be enforced, observable, testable and recoverable.

## P41 — Unified Identity & SSO Enforcement
- OAuth2/OIDC provider runtime for Google, GitHub and enterprise IdPs
- provider discovery from server-side configuration only
- authenticated session endpoint and logout
- enforced authentication mode for production
- domain-aware enterprise SSO discovery foundation
- secure callback/origin handling behind HTTPS reverse proxies
- no client secrets in frontend or repository

## P42 — Identity Authorization & Role Mapping
- normalized user identity model
- IdP group/claim to workspace role mapping
- least-privilege defaults
- admin emergency-access policy metadata
- authorization audit records
- generated-app auth profile contract

## P43 — Session & Access Security
- session lifecycle policy
- secure cookies and forwarded-header awareness
- inactivity/absolute timeout configuration
- CSRF strategy for browser/API boundaries
- logout/session invalidation
- authentication failure diagnostics without secret leakage

## P44 — Workspace Access Policy
- workspace-level SSO-required policy
- approved-email-domain policy
- invitation/access-request lifecycle
- ownership and administrator safeguards
- server-enforced access decisions
- policy simulation and audit

## P45 — Deployment Configuration & Secret Governance
- deployment-time secret injection only
- provider configuration validation
- secret-presence health diagnostics without returning values
- environment-specific auth configuration
- secret rotation readiness
- fail-closed production configuration

## P46 — Production Edge & HTTPS Readiness
- canonical public URL configuration
- HTTPS callback compatibility
- trusted proxy/forwarded header handling
- HSTS/readiness policy hooks
- secure-cookie production mode
- origin/redirect validation

## P47 — Authentication Observability
- auth readiness endpoint
- configured-provider inventory
- login/logout/failure counters and structured events
- deployment health checks for required providers
- diagnostics suitable for operations without PII/secret leakage

## P48 — Identity Regression & Security Testing
- authenticated/anonymous route regression
- OAuth provider configuration tests
- session endpoint tests
- authorization tests
- CSRF/logout behavior tests
- production configuration validation tests

## P49 — Identity Operations & Recovery
- provider-disable/fallback procedure
- emergency admin recovery metadata
- identity incident runbook
- configuration rollback path
- audit evidence for auth configuration changes
- deployment acceptance checklist

## P50 — Production Identity Acceptance
- SSO login succeeds through the configured production provider
- production routes are authenticated as designed
- provider secrets stay server-side
- public HTTPS callback/origin is validated
- health/readiness remains green after auth enforcement
- CI identity regression passes
- deployment evidence recorded
- PASS/PARTIAL/GAP acceptance result with explicit external IdP dependencies
