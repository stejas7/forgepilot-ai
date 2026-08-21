# BUG-001 — OAuth initiation blocked before provider redirect

Tracked in GitHub Issues. This file captures acceptance criteria alongside the implementation branch.

- CAPTCHA must be verified before Google/GitHub OAuth initiation.
- OAuth callback endpoints must remain reachable for the handshake.
- Application APIs must remain protected for anonymous users.
