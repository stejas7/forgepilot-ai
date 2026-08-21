import { useEffect, useMemo, useState } from 'react'

type Project = { id: string; name: string; description: string; stack: string; status: string }
type AgentResponse = { mode: string; steps: string[]; message: string }
type ConversationMessage = { id: string; projectId: string; role: 'USER'|'ASSISTANT'; mode: 'BUILD'|'PLAN'; content: string; createdAt: string }
type View = 'dashboard' | 'builder'
type Filter = 'all' | 'starred' | 'owned' | 'shared'

const suggestions = [
  ['CRM workspace', 'Build a modern CRM dashboard with contacts, pipeline, tasks and analytics.'],
  ['Booking app', 'Build a polished appointment booking app with availability, customers and reminders.'],
  ['SaaS dashboard', 'Build a SaaS admin dashboard with authentication, subscriptions, usage and team management.'],
  ['Internal tool', 'Build an internal operations tool with forms, approvals, audit history and role based access.']
]

export default function App() {
  const [projects, setProjects] = useState<Project[]>([])
  const [selected, setSelected] = useState<Project | null>(null)
  const [view, setView] = useState<View>('dashboard')
  const [filter, setFilter] = useState<Filter>('all')
  const [prompt, setPrompt] = useState('')
  const [mode, setMode] = useState<'build' | 'plan'>('build')
  const [busy, setBusy] = useState(false)
  const [activity, setActivity] = useState<string[]>([])
  const [conversation, setConversation] = useState<ConversationMessage[]>([])
  const [searchOpen, setSearchOpen] = useState(false)
  const [query, setQuery] = useState('')

  useEffect(() => { void refreshProjects() }, [])
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') { event.preventDefault(); setSearchOpen(true) }
      if (event.key === 'Escape') setSearchOpen(false)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  async function refreshProjects() {
    const response = await fetch('/api/projects')
    if (!response.ok) return
    setProjects(await response.json())
  }

  async function refreshConversation(projectId: string) {
    const response = await fetch(`/api/projects/${projectId}/conversation`)
    if (!response.ok) return
    setConversation(await response.json())
  }

  async function createAndRun() {
    if (!prompt.trim()) return
    const initial = prompt.trim()
    setBusy(true)
    try {
      const create = await fetch('/api/projects', { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ name:projectName(initial), description:initial, stack:'React + Spring Boot + PostgreSQL' }) })
      if (!create.ok) throw new Error('Unable to create project')
      const project: Project = await create.json()
      setSelected(project)
      setView('builder')
      setConversation([])
      setActivity([mode === 'plan' ? 'Creating a plan…' : 'Creating your application…'])
      await execute(project, mode, initial)
      setPrompt('')
      await refreshProjects()
    } catch (error) { setActivity([error instanceof Error ? error.message : 'Unable to start project']) }
    finally { setBusy(false) }
  }

  async function execute(project: Project, command: 'plan'|'build', text: string) {
    const response = await fetch(`/api/ai/${command}`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ projectId:project.id, prompt:text }) })
    if (!response.ok) throw new Error(`${command} command failed`)
    const result: AgentResponse = await response.json()
    setActivity(result.steps)
    await refreshConversation(project.id)
  }

  async function followUp() {
    if (!selected || !prompt.trim()) return
    const text = prompt.trim()
    setBusy(true)
    try {
      await execute(selected, mode, text)
      setPrompt('')
      await refreshProjects()
    } catch (error) { setActivity([error instanceof Error ? error.message : 'Command failed']) }
    finally { setBusy(false) }
  }

  async function openProject(project: Project) {
    setSelected(project)
    setView('builder')
    setPrompt('')
    setActivity([])
    await refreshConversation(project.id)
  }

  function projectName(text: string) { const words=text.trim().split(/\s+/).slice(0,5).join(' '); return words.length>2?words:'Untitled app' }
  const results = useMemo(()=>projects.filter(p=>`${p.name} ${p.description}`.toLowerCase().includes(query.toLowerCase())),[projects,query])

  if (view === 'builder' && selected) return <Builder project={selected} prompt={prompt} setPrompt={setPrompt} mode={mode} setMode={setMode} busy={busy} activity={activity} conversation={conversation} run={followUp} back={()=>setView('dashboard')} />

  return <div className="creator-shell">
    <Sidebar filter={filter} setFilter={setFilter} search={()=>setSearchOpen(true)} />
    <main className="creator-main">
      <section className="hero">
        <div className="tool-pill">✦ &nbsp; Connect your tools &nbsp; →</div>
        <h1>What will you build today?</h1>
        <div className="hero-composer">
          <textarea autoFocus value={prompt} onChange={e=>setPrompt(e.target.value)} placeholder="Ask ForgePilot to build an app…" onKeyDown={e=>{if((e.metaKey||e.ctrlKey)&&e.key==='Enter')void createAndRun()}} />
          <div className="composer-bottom"><button className="plus" title="Attach files">＋</button><div className="mode-switch"><button className={mode==='build'?'selected':''} onClick={()=>setMode('build')}>Build</button><button className={mode==='plan'?'selected':''} onClick={()=>setMode('plan')}>Plan</button></div><button className="mic" title="Voice input">⌁</button><button className="send" disabled={!prompt.trim()||busy} onClick={()=>void createAndRun()}>{busy?'…':'↑'}</button></div>
        </div>
        <div className="suggestions">{suggestions.map(([title,text])=><button key={title} onClick={()=>setPrompt(text)}><b>{title}</b><span>{text}</span></button>)}</div>
      </section>
      <section className="projects-area"><div className="projects-head"><div><h2>Your projects</h2><p>Continue building or start from a fresh idea.</p></div><button onClick={()=>{setPrompt('');window.scrollTo({top:0,behavior:'smooth'})}}>＋ New project</button></div>{projects.length===0?<div className="empty-projects"><b>No projects yet</b><span>Your first generated application will appear here.</span></div>:<div className="project-grid">{projects.map(project=><button className="project-card" key={project.id} onClick={()=>void openProject(project)}><div className="project-thumb"><span>✦</span><div><i></i><i></i><i></i></div></div><div className="project-info"><b>{project.name}</b><span>{project.description || project.stack}</span><small>{project.status} · {project.stack}</small></div></button>)}</div>}</section>
    </main>
    {searchOpen&&<div className="search-backdrop" onMouseDown={()=>setSearchOpen(false)}><div className="command" onMouseDown={e=>e.stopPropagation()}><input autoFocus value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search projects and commands…"/><div className="command-label">Projects</div>{results.length===0?<div className="command-empty">No matching projects</div>:results.map(project=><button key={project.id} onClick={()=>{void openProject(project);setSearchOpen(false)}}><span>▱</span><div><b>{project.name}</b><small>{project.description}</small></div></button>)}</div></div>}
  </div>
}

function Sidebar({filter,setFilter,search}:{filter:Filter;setFilter:(v:Filter)=>void;search:()=>void}) {
  return <aside className="creator-sidebar"><div className="fp-mark">F</div><button className="workspace-switch"><span>F</span><b>ForgePilot Workspace</b><em>⌄</em></button><nav><button className="nav-active">⌂ <span>Dashboard</span></button><button onClick={search}>⌕ <span>Search</span><kbd>Ctrl K</kbd></button><button>◇ <span>Resources</span></button><button>⌘ <span>Connectors</span></button></nav><div className="nav-title">Projects</div><nav>{([['all','▦','All projects'],['starred','☆','Starred'],['owned','♙','Owned by me'],['shared','♧','Shared with me']] as [Filter,string,string][]).map(([key,icon,label])=><button key={key} className={filter===key?'soft-active':''} onClick={()=>setFilter(key)}>{icon} <span>{label}</span></button>)}</nav><div className="nav-title">Recents</div><p className="muted">Your recently opened projects appear here.</p><div className="sidebar-spacer"/><button className="upgrade"><b>Internal workspace</b><span>ForgePilot AI builder</span><i>✦</i></button><div className="profile"><span>TS</span><div><b>Tejas</b><small>Workspace owner</small></div><button>⚙</button></div></aside>
}

function Builder({project,prompt,setPrompt,mode,setMode,busy,activity,conversation,run,back}:{project:Project;prompt:string;setPrompt:(v:string)=>void;mode:'build'|'plan';setMode:(v:'build'|'plan')=>void;busy:boolean;activity:string[];conversation:ConversationMessage[];run:()=>void;back:()=>void}) {
  return <div className="builder-shell"><header className="builder-top"><button onClick={back}>← Dashboard</button><b>{project.name}</b><div><button>GitHub</button><button>Share</button><button className="publish">Publish</button></div></header><aside className="builder-chat"><div className="chat-title"><b>ForgePilot</b><span>{mode==='build'?'Agent':'Plan'}</span></div><div className="chat-body">{conversation.length===0?<div className="agent-message">Start by describing the change you want.</div>:conversation.map(message=><div key={message.id} className={message.role==='USER'?'user-prompt':'agent-message'}><small>{message.mode==='PLAN'?'Plan':'Build'}</small>{message.content}</div>)}{busy&&<div className="agent-message">ForgePilot is working…</div>}{activity.length>0&&<div className="execution-steps">{activity.map(step=><span key={step}>✓ {step}</span>)}</div>}</div><div className="builder-composer"><textarea value={prompt} onChange={e=>setPrompt(e.target.value)} placeholder="Ask ForgePilot to make a change…" onKeyDown={e=>{if((e.metaKey||e.ctrlKey)&&e.key==='Enter')run()}}/><div><button onClick={()=>setMode(mode==='build'?'plan':'build')}>{mode==='build'?'Build':'Plan'}⌄</button><button className="send" disabled={busy||!prompt.trim()} onClick={run}>{busy?'…':'↑'}</button></div></div></aside><main className="live-preview"><div className="preview-top"><div><button className="active">Preview</button><button>Code</button></div><span>Desktop ▾</span><div><button>↻</button><button>↗</button></div></div><div className="preview-stage"><div className="preview-browser"><div className="fake-app"><aside><b>Acme</b><span className="active">Dashboard</span><span>Customers</span><span>Analytics</span><span>Settings</span></aside><main><small>GENERATED BY FORGEPILOT</small><h1>{project.name}</h1><p>{project.description}</p><div className="fake-stats"><div><b>1,284</b><span>Customers</span></div><div><b>87%</b><span>Completion</span></div><div><b>24</b><span>Active flows</span></div></div><section><b>Your application is ready to iterate</b><p>Continue prompting on the left. Live executable preview is the next product milestone.</p></section></main></div></div></div></main></div>
}
