# ForgePilot Bug Register

## BUG-001 — OAuth login initiation can be blocked before provider redirect

**Status:** In progress

**Area:** Authentication / SSO

**Severity:** High

**Summary:**
Anonymous users can reach the ForgePilot login page, but the Spring Security chain may block `/oauth2/authorization/google` and `/oauth2/authorization/github` before the OAuth redirect starts.

**Expected:**
After mandatory CAPTCHA verification, users should be allowed to initiate Google or GitHub OAuth and reach the provider login page.

**Actual:**
OAuth initiation can be denied by the authenticated-route authorization rule before redirecting to the provider.

**Fix in progress:**
Explicitly allow the Google/GitHub OAuth initiation routes through a CAPTCHA authorization gate and keep callback routes publicly reachable for the OAuth handshake.

**Acceptance:**
- CAPTCHA required before provider redirect
- Google OAuth initiation succeeds after CAPTCHA
- GitHub OAuth initiation succeeds after CAPTCHA
- callback routes complete successfully
- anonymous application APIs remain protected
