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
  ['Engineering','solutions/engineering','Build and modernize production software'],
  ['Product managers','solutions/product','Turn product intent into working software'],
  ['Founders','solutions/founders','Validate an MVP with real code'],
  ['Designers','solutions/designers','Move from interface concept to usable app'],
  ['Marketing','solutions/marketing','Launch campaign and content experiences'],
  ['Sales','solutions/sales','Build CRM, enablement and deal workflows'],
  ['Operations','solutions/operations','Replace manual processes with internal apps'],
  ['People','solutions/people','Create employee and HR workflows'],
  ['Websites','solutions/websites','Build responsive sites and portals'],
  ['Prototyping','solutions/prototyping','Validate ideas with working prototypes'],
  ['Internal tools','solutions/internal-tools','Build governed tools for your team']
]
const resourceLinks=[
  ['Templates','resources/templates','Start from proven application patterns'],
  ['Connectors','resources/connectors','Integrate data, APIs and external services'],
  ['Documentation','resources/docs','Understand the ForgePilot platform'],
  ['Guides','resources/guides','Build with production discipline'],
  ['Academy','resources/academy','Learn AI application engineering'],
  ['Blog','resources/blog','Product and engineering updates'],
  ['Partners','resources/partners','Extend ForgePilot with ecosystem partners'],
  ['Customer stories','resources/customer-stories','See how teams use ForgePilot']
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
        <div className="nav-group"><button>Solutions <span>⌄</span></button><div className="mega-menu solutions-menu"><div><small>WHO IS IT FOR?</small><b>Build for the way your team works.</b><p>ForgePilot turns requirements into governed, production-oriented software.</p></div><div className="mega-links">{solutionLinks.map(([label,slug,desc])=><button key={slug} onClick={()=>navigate(slug)}><b>{label}</b><span>{desc}</span></button>)}</div></div></div>
        <div className="nav-group"><button>Resources <span>⌄</span></button><div className="mega-menu resources-menu"><div><small>RESOURCES</small><b>Learn, extend and operate ForgePilot.</b><p>Patterns, integrations and guidance for the complete creator journey.</p></div><div className="mega-links">{resourceLinks.map(([label,slug,desc])=><button key={slug} onClick={()=>navigate(slug)}><b>{label}</b><span>{desc}</span></button>)}</div></div></div>
        <button onClick={()=>navigate('community')}>Community</button>
        <button onClick={()=>navigate('enterprise')}>Enterprise</button>
        <button onClick={()=>navigate('pricing')}>Pricing</button>
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

      <section id="capabilities" className="public-section"><div className="section-heading"><span>One platform</span><h2>Build, connect and operate—not just generate.</h2><p>ForgePilot keeps product planning, code generation, backend capabilities, integrations, verification and delivery connected to the same project context.</p></div><div className="public-grid three"><article><span>01</span><h3>Plan & build</h3><p>Turn a prompt into architecture, code, preview and recoverable versions.</p></article><article><span>02</span><h3>Backend & integrations</h3><p>Generate APIs, authentication, database structures, file storage and project-scoped connectors.</p></article><article><span>03</span><h3>Secure & ship</h3><p>Keep source ownership, governance, security checks, publish controls and deployment evidence visible.</p></article></div></section>
      <section id="workflow" className="public-section public-dark"><div className="section-heading"><span>How it works</span><h2>Prompt → Plan → Build → Preview → Validate → GitHub → Deploy</h2></div><div className="workflow-line">{['Describe','Plan','Build','Preview','Validate','GitHub','Deploy'].map((item,index)=><div key={item}><b>{String(index+1).padStart(2,'0')}</b><span>{item}</span></div>)}</div></section>
      <section className="public-section"><div className="section-heading"><span>Building and beyond</span><h2>A product foundation that grows with the app.</h2></div><div className="public-grid three"><article><span>Cloud</span><h3>Deployment-ready delivery</h3><p>Build toward repeatable deployment, environment controls and observable releases.</p></article><article><span>Connectors</span><h3>Your stack, connected</h3><p>Bring APIs, databases, email, payments and automation into project context.</p></article><article><span>Application backend</span><h3>Data, auth and storage</h3><p>Generate backend code, role-aware authentication, database structures and object storage scaffolding.</p></article><article><span>Security</span><h3>Guardrails by workflow</h3><p>Keep secrets server-side, apply role-aware controls and surface verification before publishing.</p></article><article><span>Source ownership</span><h3>GitHub-connected delivery</h3><p>Keep generated work reviewable and transferable through standard source control.</p></article><article><span>Responsive</span><h3>Build across devices</h3><p>Preview and refine experiences for desktop, tablet and mobile from the same workspace.</p></article></div></section>
      <section id="enterprise" className="public-section split-section"><div><span className="section-label">Enterprise-ready foundation</span><h2>AI speed with engineering control.</h2><p>Workspace roles, controlled publishing, auditability, connector governance and source ownership are designed into the product rather than added after generation.</p><button className="public-secondary" onClick={()=>navigate('enterprise')}>Explore Enterprise →</button></div><div className="proof-card"><b>Governed creator journey</b><ul><li>SSO-backed workspace access</li><li>Role-aware creator permissions</li><li>Security and publish gates</li><li>GitHub ownership and deployment evidence</li></ul></div></section>
      <section id="security" className="public-section split-section security-section"><div><span className="section-label">Security</span><h2>Secure by workflow, transparent by evidence.</h2><p>ForgePilot separates implemented controls from roadmap claims and keeps credentials server-side. Security checks remain part of the build and publish flow.</p><button className="public-secondary" onClick={()=>navigate('security')}>Explore Security →</button></div><div className="security-badges"><span>OAuth / SSO</span><span>RBAC</span><span>Secret isolation</span><span>Audit trail</span><span>Security scans</span><span>Release gates</span></div></section>
    </main></>}

    <footer className="public-footer"><button className="public-brand brand-button" onClick={home}><span className="pilot-mark small" aria-hidden="true"><i/><i/><i/></span><b>ForgePilot</b></button><p>AI application engineering with production discipline.</p><button onClick={onAuthenticate}>Log in</button></footer>
  </div>
}

function routeFromHash(){return window.location.hash.replace(/^#\/?/,'').replace(/\/$/,'')}
