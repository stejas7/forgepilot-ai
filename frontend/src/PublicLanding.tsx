import {useState} from 'react'

type Props={onAuthenticate:()=>void}

const examples=[
  'Build an internal CRM with contacts, pipeline and activity tracking',
  'Create a customer support portal with authentication and analytics',
  'Plan a SaaS dashboard with billing, teams and usage limits'
]

export default function PublicLanding({onAuthenticate}:Props){
  const [prompt,setPrompt]=useState('')
  function start(){
    const value=prompt.trim()
    if(value) sessionStorage.setItem('forgepilot.pendingPrompt',value)
    onAuthenticate()
  }
  return <div className="public-shell">
    <header className="public-header">
      <a className="public-brand" href="#top" aria-label="ForgePilot home"><span className="pilot-mark" aria-hidden="true"><i/><i/><i/></span><b>ForgePilot</b></a>
      <nav aria-label="Public navigation">
        <a href="#capabilities">Product</a>
        <a href="#workflow">How it works</a>
        <a href="#enterprise">Enterprise</a>
        <a href="#security">Security</a>
      </nav>
      <div className="public-actions"><button className="public-login" onClick={onAuthenticate}>Log in</button><button className="public-primary" onClick={onAuthenticate}>Get started</button></div>
    </header>

    <main id="top">
      <section className="public-hero">
        <div className="public-kicker">AI application engineering, from idea to production</div>
        <h1>Describe the product.<br/>ForgePilot engineers the path.</h1>
        <p>Plan, build, review, secure and ship production applications from one AI-native engineering workspace.</p>
        <div className="public-prompt-card">
          <textarea value={prompt} onChange={e=>setPrompt(e.target.value)} placeholder="What do you want to build?" aria-label="Describe what you want ForgePilot to build"/>
          <div className="public-prompt-footer"><span>Start with an idea — refine it after sign in.</span><button className="public-primary" disabled={!prompt.trim()} onClick={start}>Build with ForgePilot →</button></div>
        </div>
        <div className="public-examples">{examples.map(example=><button key={example} onClick={()=>setPrompt(example)}>{example}</button>)}</div>
      </section>

      <section id="capabilities" className="public-section">
        <div className="section-heading"><span>Product</span><h2>One engineering loop, not a pile of AI demos.</h2><p>ForgePilot keeps planning, implementation, verification and delivery connected to the same project context.</p></div>
        <div className="public-grid three">
          <article><span>01</span><h3>Plan before changing code</h3><p>Turn a prompt into architecture, scope and executable steps before implementation starts.</p></article>
          <article><span>02</span><h3>Build with verification</h3><p>Generate and iterate on real code with preview, tests, security checks and recoverable versions.</p></article>
          <article><span>03</span><h3>Own the outcome</h3><p>Connect GitHub, integrations and deployment while keeping governance and engineering evidence visible.</p></article>
        </div>
      </section>

      <section id="workflow" className="public-section public-dark">
        <div className="section-heading"><span>How it works</span><h2>Prompt → Plan → Build → Verify → Ship</h2></div>
        <div className="workflow-line">{['Describe','Plan','Build','Preview','Validate','GitHub','Deploy'].map((item,index)=><div key={item}><b>{String(index+1).padStart(2,'0')}</b><span>{item}</span></div>)}</div>
      </section>

      <section id="enterprise" className="public-section split-section"><div><span className="section-label">Enterprise-ready foundation</span><h2>AI speed with engineering control.</h2><p>Workspace roles, controlled publishing, auditability, connector governance and source ownership are designed into the product rather than added after generation.</p><button className="public-secondary" onClick={onAuthenticate}>Open workspace →</button></div><div className="proof-card"><b>Governed creator journey</b><ul><li>SSO-backed workspace access</li><li>Role-aware creator permissions</li><li>Security and publish gates</li><li>GitHub ownership and deployment evidence</li></ul></div></section>

      <section id="security" className="public-section split-section security-section"><div><span className="section-label">Security</span><h2>Secure by workflow, transparent by evidence.</h2><p>ForgePilot separates implemented controls from roadmap claims and keeps credentials server-side. Security checks remain part of the build and publish flow.</p></div><div className="security-badges"><span>OAuth / SSO</span><span>RBAC</span><span>Secret isolation</span><span>Audit trail</span><span>Security scans</span><span>Release gates</span></div></section>
    </main>

    <footer className="public-footer"><a className="public-brand" href="#top"><span className="pilot-mark small" aria-hidden="true"><i/><i/><i/></span><b>ForgePilot</b></a><p>AI application engineering with production discipline.</p><button onClick={onAuthenticate}>Log in</button></footer>
  </div>
}
