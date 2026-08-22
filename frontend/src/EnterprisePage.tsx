type Props={onAuthenticate:()=>void;onHome:()=>void}

const capabilities=[
  ['Governed creation','Workspace roles, project ownership and approval-aware publishing keep AI-generated work inside an accountable engineering workflow.'],
  ['Source ownership','Projects can be connected to GitHub so teams retain normal code review, branch and repository ownership patterns.'],
  ['Enterprise identity','OAuth/SSO-backed access is the current foundation; broader enterprise identity and provisioning remain explicit roadmap items until implemented.'],
  ['Security gates','Security review, secrets handling and release checks are positioned as evidence-bearing workflow controls rather than marketing badges.'],
  ['Connector governance','Project-scoped integrations keep credentials and external system access controlled and observable.'],
  ['Deployment discipline','Immutable builds, CI validation, deployment verification and rollback-oriented release practices are part of the operating model.']
]

export default function EnterprisePage({onAuthenticate,onHome}:Props){
  function start(){sessionStorage.setItem('forgepilot.pendingPrompt','Plan an enterprise application with SSO, role-based access, audit logs, GitHub ownership, security gates and controlled publishing.');onAuthenticate()}
  return <main className="enterprise-page">
    <button className="content-back" onClick={onHome}>← ForgePilot home</button>
    <section className="enterprise-hero"><span>ForgePilot Enterprise</span><h1>Move fast with AI.<br/>Keep engineering control.</h1><p>ForgePilot is designed for teams that want AI-assisted application delivery without giving up source ownership, governance, security review or production discipline.</p><div><button className="public-primary" onClick={start}>Start enterprise build →</button><button className="public-secondary" onClick={onAuthenticate}>Open workspace</button></div></section>
    <section className="enterprise-story"><div><span>From idea to governed release</span><h2>One creator journey across product, engineering and operations.</h2></div><ol>{['Describe and plan','Generate and iterate','Review working preview','Validate code and security','Sync source ownership','Publish with evidence'].map((item,index)=><li key={item}><b>{String(index+1).padStart(2,'0')}</b><span>{item}</span></li>)}</ol></section>
    <section className="enterprise-grid">{capabilities.map(([title,body])=><article key={title}><h3>{title}</h3><p>{body}</p></article>)}</section>
    <section className="enterprise-proof"><div><span>Enterprise principle</span><h2>No invisible control plane.</h2><p>ForgePilot should make identity, permissions, source changes, connector access, security checks and releases inspectable by the teams responsible for them.</p></div><div className="proof-stack"><span>Identity</span><span>RBAC</span><span>GitHub</span><span>Audit</span><span>Security</span><span>Release</span></div></section>
    <section className="enterprise-cta"><span>Build with your governance model in mind</span><h2>Start with the requirements. ForgePilot carries them into the engineering workflow.</h2><button className="public-primary" onClick={start}>Plan an enterprise app →</button></section>
  </main>
}
