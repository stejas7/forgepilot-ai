import {useEffect,useState} from 'react'
import PublicContentPage,{hasPublicPage} from './PublicContentPage'
import EnterprisePage from './EnterprisePage'
import SecurityTrustPage from './SecurityTrustPage'

type Props={onAuthenticate:()=>void}
type Mode='build'|'plan'

const examples=[
  'Build an internal CRM with contacts, pipeline and activity tracking',
  'Create a customer support portal with authentication and analytics',
  'Plan a SaaS dashboard with billing, teams and usage limits'
]

const solutionLinks=[
  ['Engineering','solutions/engineering'],['Product','solutions/product'],['Founders','solutions/founders'],['Operations','solutions/operations'],['Websites','solutions/websites'],['Internal tools','solutions/internal-tools']
]
const resourceLinks=[
  ['Templates','resources/templates'],['Connectors','resources/connectors'],['Documentation','resources/docs'],['Guides','resources/guides']
]

export default function PublicLanding({onAuthenticate}:Props){
  const [prompt,setPrompt]=useState(''),[mode,setMode]=useState<Mode>('build'),[route,setRoute]=useState(()=>routeFromHash())
  useEffect(()=>{const onHash=()=>setRoute(routeFromHash());window.addEventListener('hashchange',onHash);return()=>window.removeEventListener('hashchange',onHash)},[])
  function start(){const value=prompt.trim();if(value){sessionStorage.setItem('forgepilot.pendingPrompt',value);sessionStorage.setItem('forgepilot.pendingMode',mode)}onAuthenticate()}
  function navigate(slug:string){window.location.hash=`/${slug}`}
  function home(){history.replaceState(null,'',window.location.pathname+window.location.search);setRoute('')}
  const content=hasPublicPage(route)
  return <div className="public-shell">
    <header className="public-header">
      <button className="public-brand brand-button" onClick={home} aria-label="ForgePilot home"><span className="pilot-mark" aria-hidden="true"><i/><i/><i/></span><b>ForgePilot</b></button>
      <nav aria-label="Public navigation">
        <div className="nav-group"><button>Solutions <span>⌄</span></button><div className="mega-menu"><div><small>SOLUTIONS</small><b>Build for the way your team works.</b><p>Real app-building workflows for engineering, product and operations.</p></div><div className="mega-links">{solutionLinks.map(([label,slug])=><button key={slug} onClick={()=>navigate(slug)}><b>{label}</b><span>Explore ForgePilot for {label.toLowerCase()}</span></button>)}</div></div></div>
        <div className="nav-group"><button>Resources <span>⌄</span></button><div className="mega-menu resources-menu"><div><small>RESOURCES</small><b>Patterns, integrations and guidance.</b><p>Everything needed to understand and extend the ForgePilot creator journey.</p></div><div className="mega-links">{resourceLinks.map(([label,slug])=><button key={slug} onClick={()=>navigate(slug)}><b>{label}</b><span>Open {label.toLowerCase()}</span></button>)}</div></div></div>
        <button onClick={()=>{home();setTimeout(()=>document.getElementById('workflow')?.scrollIntoView({behavior:'smooth'}),0)}}>How it works</button>
        <button onClick={()=>navigate('enterprise')}>Enterprise</button>
        <button onClick={()=>navigate('security')}>Security</button>
      </nav>
      <div className="public-actions"><button className="public-login" onClick={onAuthenticate}>Log in</button><button className="public-primary" onClick={onAuthenticate}>Get started</button></div>
    </header>

    {route==='enterprise'?<EnterprisePage onAuthenticate={onAuthenticate} onHome={home}/>:route==='security'?<SecurityTrustPage onAuthenticate={onAuthenticate} onHome={home}/>:content?<PublicContentPage slug={route} onAuthenticate={onAuthenticate} onHome={home}/>:<>
    <main id="top">
      <section className="public-hero">
        <div className="public-kicker">AI application engineering, from idea to production</div>
        <h1>Describe the product.<br/>ForgePilot engineers the path.</h1>
        <p>Plan, build, review, secure and ship production applications from one AI-native engineering workspace.</p>
        <div className="public-prompt-card">
          <textarea value={prompt} onChange={e=>setPrompt(e.target.value)} placeholder={mode==='plan'?'What should ForgePilot plan?':'What do you want ForgePilot to build?'} aria-label="Describe what you want ForgePilot to do"/>
          <div className="public-prompt-footer"><div className="public-mode-switch" aria-label="Choose Build or Plan mode"><button className={mode==='build'?'selected':''} onClick={()=>setMode('build')}>Build</button><button className={mode==='plan'?'selected':''} onClick={()=>setMode('plan')}>Plan</button></div><span>{mode==='plan'?'Create architecture and implementation steps before code.':'Create the project and start implementation after sign in.'}</span><button className="public-primary" disabled={!prompt.trim()} onClick={start}>{mode==='plan'?'Create plan':'Build app'} →</button></div>
        </div>
        <div className="public-examples">{examples.map(example=><button key={example} onClick={()=>{setPrompt(example);setMode(example.startsWith('Plan')?'plan':'build')}}>{example}</button>)}</div>
      </section>

      <section id="capabilities" className="public-section"><div className="section-heading"><span>Product</span><h2>One engineering loop, not a pile of AI demos.</h2><p>ForgePilot keeps planning, implementation, verification and delivery connected to the same project context.</p></div><div className="public-grid three"><article><span>01</span><h3>Plan before changing code</h3><p>Turn a prompt into architecture, scope and executable steps before implementation starts.</p></article><article><span>02</span><h3>Build with verification</h3><p>Generate and iterate on real code with preview, tests, security checks and recoverable versions.</p></article><article><span>03</span><h3>Own the outcome</h3><p>Connect GitHub, integrations and deployment while keeping governance and engineering evidence visible.</p></article></div></section>
      <section id="workflow" className="public-section public-dark"><div className="section-heading"><span>How it works</span><h2>Prompt → Plan → Build → Verify → Ship</h2></div><div className="workflow-line">{['Describe','Plan','Build','Preview','Validate','GitHub','Deploy'].map((item,index)=><div key={item}><b>{String(index+1).padStart(2,'0')}</b><span>{item}</span></div>)}</div></section>
      <section id="enterprise" className="public-section split-section"><div><span className="section-label">Enterprise-ready foundation</span><h2>AI speed with engineering control.</h2><p>Workspace roles, controlled publishing, auditability, connector governance and source ownership are designed into the product rather than added after generation.</p><button className="public-secondary" onClick={()=>navigate('enterprise')}>Explore Enterprise →</button></div><div className="proof-card"><b>Governed creator journey</b><ul><li>SSO-backed workspace access</li><li>Role-aware creator permissions</li><li>Security and publish gates</li><li>GitHub ownership and deployment evidence</li></ul></div></section>
      <section id="security" className="public-section split-section security-section"><div><span className="section-label">Security</span><h2>Secure by workflow, transparent by evidence.</h2><p>ForgePilot separates implemented controls from roadmap claims and keeps credentials server-side. Security checks remain part of the build and publish flow.</p><button className="public-secondary" onClick={()=>navigate('security')}>Explore Security →</button></div><div className="security-badges"><span>OAuth / SSO</span><span>RBAC</span><span>Secret isolation</span><span>Audit trail</span><span>Security scans</span><span>Release gates</span></div></section>
    </main></>}

    <footer className="public-footer"><button className="public-brand brand-button" onClick={home}><span className="pilot-mark small" aria-hidden="true"><i/><i/><i/></span><b>ForgePilot</b></button><p>AI application engineering with production discipline.</p><button onClick={onAuthenticate}>Log in</button></footer>
  </div>
}

function routeFromHash(){return window.location.hash.replace(/^#\/?/,'').replace(/\/$/,'')}
