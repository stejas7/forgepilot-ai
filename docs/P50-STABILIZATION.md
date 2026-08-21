# P50 Stabilization Gate

P50 is complete only when every mandatory gate below is PASS. A coded feature is not enough.

## Identity scope
- Google OAuth only when Google credentials are configured.
- GitHub OAuth only when GitHub credentials are configured.
- No generic enterprise OIDC provider and no CAPTCHA.
- ForgePilot stores no local passwords; Forgot Password routes to provider recovery.

## Mandatory gates
1. Backend `mvn clean verify` passes, including OAuth registration, startup validation, role mapping, and workspace access tests.
2. Frontend typecheck and production build pass.
3. Anonymous application APIs remain protected while `/oauth2/authorization/google`, `/oauth2/authorization/github`, and OAuth callbacks are reachable.
4. `/api/auth/providers` advertises only configured Google/GitHub providers.
5. `/api/auth/readiness` reports no secret values and is ready for production configuration.
6. Successful login establishes a session; `/api/auth/me` returns normalized identity and roles.
7. Workspace domain/admin policy is enforced server-side.
8. Logout invalidates the session and clears the session cookie.
9. Production public URL is HTTPS and forwarded headers preserve OAuth callback scheme/host.
10. Deployment smoke test confirms health, login page, provider redirect initiation, callback, authenticated UI/API, and logout.

## Stabilization rule
Any failed gate is a blocker. Fix blockers before adding post-P50 product features. Record runtime-only provider/callback failures as GitHub bugs with evidence, fix, rerun the affected gate, and close only after verification.

## Final evidence
Record PASS/PARTIAL/GAP for Google login, GitHub login, session, roles, access policy, logout, readiness, CI, HTTPS callback, and production smoke test. P50 may be marked complete only when all mandatory rows are PASS.
