# ForgePilot Identity Operations

## Supported providers
ForgePilot production authentication supports Google and GitHub only. ForgePilot stores no local passwords.

## Normal login
1. Open `https://forgepilot-ai.duckdns.org`.
2. Choose Google or GitHub.
3. Complete provider authentication.
4. ForgePilot validates workspace access policy and role mapping.
5. The authenticated session is established and the dashboard opens.

## Forgot password / account recovery
Use the provider-managed recovery flow exposed from the login page. ForgePilot never receives or resets provider passwords.

## Provider outage or bad credential recovery
1. Do not disable production authentication globally as a first response.
2. Rotate or correct the affected GitHub Actions provider secret.
3. Keep the unaffected provider configured when possible.
4. Redeploy the last known-good application image/configuration.
5. Confirm `/actuator/health` and `/api/auth/readiness` before declaring recovery.

## Emergency administrator access
Emergency access is controlled through `FORGEPILOT_ADMIN_EMAILS`. Changes must be temporary, reviewed, and removed after recovery.

## Rollback
Use the previous immutable GHCR image SHA and the previous known-good deployment configuration. Never copy OAuth secrets into source control or frontend code.

## P50 acceptance checklist
- Google login initiation reaches Google when configured.
- GitHub login initiation reaches GitHub when configured.
- OAuth callback returns to the HTTPS ForgePilot origin.
- Anonymous application APIs are blocked when OAuth is enabled.
- Workspace approved-domain policy is enforced server-side.
- Role mapping defaults to least privilege.
- Logout invalidates the session and removes the session cookie.
- `/api/auth/readiness` exposes no secret values.
- Forgot Password opens provider-managed recovery options.
- CI identity regression passes.
- Production health remains green after auth enforcement.
