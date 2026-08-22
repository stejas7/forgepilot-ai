# P50 Stabilization Gate

P50 is complete only when every mandatory gate below is PASS. A coded feature is not enough.

## Identity scope
- Google OAuth only when Google credentials are configured.
- GitHub OAuth only when GitHub credentials are configured.
- Login and Sign Up are both functional SSO entry points.
- First successful Google/GitHub authentication provisions ForgePilot access for a new user; returning users use the same OAuth identity for Login.
- No generic enterprise OIDC provider and no CAPTCHA.
- ForgePilot stores no local passwords; Forgot Password routes to Google/GitHub provider recovery.

## Mandatory gates
1. Backend `mvn clean verify` passes, including OAuth registration, startup validation, role mapping, and workspace access tests.
2. Frontend typecheck and production build pass.
3. Login with Google and Login with GitHub both initiate their configured OAuth flows.
4. Sign Up with Google and Sign Up with GitHub both initiate OAuth and establish a valid first-time ForgePilot session when provider authentication succeeds.
5. Forgot Password exposes working Google Account Recovery and GitHub password-reset actions without requesting a ForgePilot password.
6. Anonymous application APIs remain protected while `/oauth2/authorization/google`, `/oauth2/authorization/github`, and OAuth callbacks are reachable.
7. `/api/auth/providers` advertises only configured Google/GitHub providers.
8. `/api/auth/readiness` reports no secret values and is ready for production configuration.
9. Successful authentication establishes a session; `/api/auth/me` returns normalized identity and roles.
10. Workspace domain/admin policy is enforced server-side.
11. Logout invalidates the session and clears the session cookie.
12. Production public URL is HTTPS and forwarded headers preserve OAuth callback scheme/host.
13. Deployment smoke test confirms health, Login, Sign Up, Forgot Password, provider redirects, callback, authenticated UI/API, and logout.

## Stabilization rule
Any failed gate is a blocker. Fix blockers before adding post-P50 product features. Record runtime-only provider/callback failures as GitHub bugs with evidence, fix, rerun the affected gate, and close only after verification.

## Final evidence
Record PASS/PARTIAL/GAP for Google Login, GitHub Login, Google Sign Up, GitHub Sign Up, Forgot Password, session, roles, access policy, logout, readiness, CI, HTTPS callback, and production smoke test. P50 may be marked complete only when all mandatory rows are PASS.
