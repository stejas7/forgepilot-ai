type Props={onAuthenticate:()=>void;onHome:()=>void}

const controls=[
  ['Identity & access','Google/GitHub OAuth, workspace authentication and role-aware access are part of the current product foundation.'],
  ['Secrets handling','Provider credentials and project secrets stay server-side and are never embedded into the React bundle.'],
  ['Application isolation','Project-scoped data, connector configuration and runtime boundaries are treated as security controls, not UI conventions.'],
  ['Security review','ForgePilot includes security review surfaces and publish-gate concepts so findings can block release when policy requires it.'],
  ['Auditability','Important identity, workspace, connector, change and publishing activity is designed to remain attributable and reviewable.'],
  ['Release discipline','CI verification, immutable image publishing, deployment health checks and rollback-oriented delivery reduce release ambiguity.']
]

export default function SecurityTrustPage({onAuthenticate,onHome}:Props){
  function start(){sessionStorage.setItem('forgepilot.pendingPrompt','Plan a secure internal application with SSO, RBAC, secret isolation, audit logging, security scanning and controlled publishing.');onAuthenticate()}
  return <main className="security-page">
    <button className="content-back" onClick={onHome}>← ForgePilot home</button>
    <section className="security-hero"><span>Security & Trust</span><h1>Security should be visible in the workflow.</h1><p>ForgePilot treats authentication, permissions, secrets, project boundaries, validation and release evidence as engineering controls. We only claim controls that are actually implemented or explicitly identified as roadmap work.</p><div><button className="public-primary" onClick={start}>Plan a secure app →</button><button className="public-secondary" onClick={onAuthenticate}>Open workspace</button></div></section>
    <section className="trust-status"><div><span>Current foundation</span><b>OAuth / SSO-backed sign-in</b><b>Role-aware workspace access</b><b>Server-side secrets</b><b>CI and deployment verification</b></div><div><span>Roadmap, not a current certification claim</span><b>Enterprise SAML / SCIM depth</b><b>Formal compliance certifications</b><b>Advanced organization-wide policy controls</b><b>External assurance evidence</b></div></section>
    <section className="security-control-grid">{controls.map(([title,body])=><article key={title}><h2>{title}</h2><p>{body}</p></article>)}</section>
    <section className="security-principles"><div><span>Principle 01</span><h2>Least privilege over convenient defaults.</h2><p>Creator permissions and administrative authority should remain explicit and independently testable.</p></div><div><span>Principle 02</span><h2>Evidence over badges.</h2><p>Security posture should be supported by inspectable controls, tests, audit records and operational evidence.</p></div><div><span>Principle 03</span><h2>Fail closed at release boundaries.</h2><p>Missing secrets, unhealthy deployments and failed verification should stop release rather than silently degrade.</p></div></section>
    <section className="enterprise-cta"><span>Build securely from the first prompt</span><h2>Put access, data and release requirements into the plan before code changes begin.</h2><button className="public-primary" onClick={start}>Start secure planning →</button></section>
  </main>
}
