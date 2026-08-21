# ForgePilot AI

ForgePilot AI is an internal AI application engineering platform: describe a product in natural language, plan it, generate and edit code, run it safely, preview it, test it, version it, sync it to GitHub, and deploy it.

> This project independently implements product-building workflows. It does not copy third-party proprietary source code, prompts, branding, or assets.

## Product goal

**Login / SSO → Workspace → Prompt → Plan → Build → Preview → Validate → Version → GitHub → Deploy**

## Authentication & SSO

ForgePilot now has a dedicated login experience and a Spring Security OAuth2/OIDC foundation. The target authentication surface supports work-email sign-in plus Google, GitHub and enterprise SSO. Enterprise identity is designed for OIDC/SAML-backed providers such as Microsoft Entra ID, Okta and Google Workspace, with SCIM and workspace role mapping tracked in the enterprise roadmap.

OAuth/OIDC credentials must remain server-side. Never put client secrets in the React bundle or commit them to Git. Provider registrations should be supplied through deployment configuration/secrets. Standard Spring Security callback routes are `/login/oauth2/code/{registrationId}` and authorization starts at `/oauth2/authorization/{registrationId}`.

Recommended deployment secret names:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
GITHUB_OAUTH_CLIENT_ID
GITHUB_OAUTH_CLIENT_SECRET
SSO_CLIENT_ID
SSO_CLIENT_SECRET
SSO_ISSUER_URI
```

The provider-discovery endpoint is `GET /api/auth/providers`; the authenticated-session endpoint is `GET /api/auth/me`. Providers should only be registered in the deployed Spring configuration when their credentials are present, so a missing enterprise IdP cannot break application startup.

## Architecture

- Java 21 + Spring Boot platform API
- Spring Security + OAuth2/OIDC authentication foundation
- React/TypeScript builder UI and dedicated login experience
- PostgreSQL persistence
- Redis for execution/session coordination
- Isolated Docker/Kubernetes execution sandboxes
- Multi-model AI gateway and agent orchestrator
- GitHub + CI/CD + AWS EC2 integration

## Delivery rule

`main` must remain releasable. CI verifies every push/PR. Production deployment is triggered only from a successful build and verifies application health before declaring success.

## Roadmap

See [ROADMAP.md](ROADMAP.md). The end-state targets the full internal app-builder capability set: plan/build modes, chat, code editor, live preview, visual editing, file context, version history, rollback, generated backends/databases/auth/storage, testing, repair loops, security review, Git sync, deployments, templates, reusable skills/knowledge, subagents, collaboration, RBAC, audit logs, MCP/integrations, design systems, usage controls and administration.

## Local development

```bash
mvn -B verify
mvn spring-boot:run
```

Health: `GET /actuator/health`  
Platform status: `GET /api/platform/status`

<!-- rebuild marker: 2026-08-21 -->
