# ForgePilot AI

ForgePilot AI is an internal AI application engineering platform: describe a product in natural language, plan it, generate and edit code, run it safely, preview it, test it, version it, sync it to GitHub, and deploy it.

> This project independently implements product-building workflows. It does not copy third-party proprietary source code, prompts, branding, or assets.

## Product goal

**Prompt → Plan → Build → Preview → Validate → Version → GitHub → Deploy**

## Architecture

- Java 21 + Spring Boot platform API
- React/TypeScript builder UI (introduced incrementally behind the same API)
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
