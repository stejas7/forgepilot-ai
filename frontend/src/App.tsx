import { useEffect, useState } from 'react'

type Project = { id: string; name: string; description: string; stack: string; status: string }
type AgentResponse = { mode: string; steps: string[]; message: string }
type WorkspaceFile = { path: string; content: string }
type WorkspaceVersion = { id: string; label: string; createdAt: string; files: Record<string, string> }
type Tab = 'Preview' | 'Code' | 'Database' | 'Versions' | 'Security' | 'Deploy'

const initialPrompt = 'Build a customer onboarding application with authentication, dashboard, workflow and audit history.'

export default function App() {
  const [projects, setProjects] = useState<Project[]>([])
  const [selected, setSelected] = useState<Project | null>(null)
  const [prompt, setPrompt] = useState(initialPrompt)
  const [activity, setActivity] = useState<string[]>(['Create or select a project to begin.'])
  const [busy, setBusy] = useState(false)
  const [built, setBuilt] = useState(false)
  const [tab, setTab] = useState<Tab>('Preview')
  const [workspaceFiles, setWorkspaceFiles] = useState<WorkspaceFile[]>([])
  const [versions, setVersions] = useState<WorkspaceVersion[]>([])
  const [selectedFile, setSelectedFile] = useState('')
  const [inspect, setInspect] = useState(false)

  useEffect(() => { void refreshProjects() }, [])
  useEffect(() => { if (selected) void refreshWorkspace(selected.id) }, [selected])

  async function refreshProjects() {
    const response = await fetch('/api/projects')
    if (!response.ok) return
    const data: Project[] = await response.json()
    setProjects(data)
    if (!selected && data.length > 0) setSelected(data[0])
  }

  async function refreshWorkspace(projectId: string) {
    const [filesResponse, versionsResponse] = await Promise.all([
      fetch(`/api/projects/${projectId}/workspace/files`),
      fetch(`/api/projects/${projectId}/workspace/versions`)
    ])
    if (filesResponse.ok) {
      const data: WorkspaceFile[] = await filesResponse.json()
      setWorkspaceFiles(data)
      setBuilt(data.length > 0)
      if (data.length > 0 && !data.some(file => file.path === selectedFile)) setSelectedFile(data[0].path)
    }
    if (versionsResponse.ok) setVersions(await versionsResponse.json())
  }

  async function createProject() {
    setBusy(true)
    try {
      const response = await fetch('/api/projects', { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ name:`Untitled app ${projects.length + 1}`, description:'Created from ForgePilot AI', stack:'React + Spring Boot + PostgreSQL' }) })
      if (!response.ok) throw new Error('Unable to create project')
      const project: Project = await response.json()
      setSelected(project); setBuilt(false); setWorkspaceFiles([]); setVersions([]); setSelectedFile(''); setActivity([`Project ${project.name} created.`]); await refreshProjects()
    } catch (error) { setActivity([error instanceof Error ? error.message : 'Project creation failed']) }
    finally { setBusy(false) }
  }

  async function run(mode: 'plan' | 'build') {
    if (!selected || !prompt.trim()) return
    setBusy(true)
    try {
      const response = await fetch(`/api/ai/${mode}`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ projectId:selected.id, prompt }) })
      if (!response.ok) throw new Error(`${mode} command failed`)
      const result: AgentResponse = await response.json()
      setActivity([result.message, ...result.steps])
      if (mode === 'build') { setBuilt(true); setTab('Preview'); await refreshWorkspace(selected.id) }
      await refreshProjects()
    } catch (error) { setActivity([error instanceof Error ? error.message : 'Command failed']) }
    finally { setBusy(false) }
  }

  async function restoreVersion(versionId: string) {
    if (!selected) return
    setBusy(true)
    try {
      const response = await fetch(`/api/projects/${selected.id}/workspace/versions/${versionId}/restore`, { method:'POST' })
      if (!response.ok) throw new Error('Unable to restore version')
      await refreshWorkspace(selected.id)
      setActivity(['Version restored successfully.', ...activity])
      setTab('Code')
    } catch (error) { setActivity([error instanceof Error ? error.message : 'Restore failed']) }
    finally { setBusy(false) }
  }

  const source = workspaceFiles.find(file => file.path === selectedFile)?.content ?? '// Run Build to generate project files.'

  function content() {
    if (tab === 'Code') return <div className="code-workspace"><div className="file-tree">{workspaceFiles.length===0?<div className="file">No generated files yet</div>:workspaceFiles.map(file=><button key={file.path} className={file.path===selectedFile?'file active':'file'} onClick={()=>setSelectedFile(file.path)}>▹ {file.path}</button>)}</div><pre className="code-editor">{source}</pre></div>
    if (tab === 'Database') return <div className="tool-page"><h2>Database</h2><p>Generated schema workspace</p><div className="metric-grid"><div><b>{workspaceFiles.filter(file=>file.path.endsWith('.sql')).length}</b><span>Schema files</span></div><div><b>PostgreSQL</b><span>Target database</span></div><div><b>{built?'Ready':'Pending'}</b><span>Workspace state</span></div></div>{workspaceFiles.filter(file=>file.path.endsWith('.sql')).map(file=><div className="schema-card" key={file.path}><b>{file.path}</b><span>{file.content.split('\n')[0]}</span></div>)}</div>
    if (tab === 'Versions') return <div className="tool-page"><h2>Version history</h2><p>Every AI build creates a recoverable workspace snapshot.</p>{versions.length===0?<div className="check">No snapshots yet. Run Build first.</div>:versions.map(version=><div className="version-row" key={version.id}><div><b>{version.label}</b><span>{new Date(version.createdAt).toLocaleString()} · {Object.keys(version.files).length} files</span></div><button onClick={()=>void restoreVersion(version.id)} disabled={busy}>Restore</button></div>)}</div>
    if (tab === 'Security') return <div className="tool-page"><h2>Security</h2><p>Automated application checks</p><div className="check good">✓ API keys stay server-side</div><div className="check good">✓ Generated workspace is project-isolated</div><div className="check good">✓ Version rollback available</div><div className="check good">✓ Deployment waits for green CI</div></div>
    if (tab === 'Deploy') return <div className="tool-page"><h2>Publish</h2><p>ForgePilot runtime deployment</p><div className="deploy-card"><b>EC2 production</b><span>Containerized deployment · Host port 8090</span><button disabled={!built}>Publish latest</button></div></div>
    return <div className="preview-canvas"><div className="browser-frame"><div className="browser-bar"><span>● ● ●</span><div>preview.forgepilot.app</div><button onClick={()=>setInspect(!inspect)}>Inspect</button></div>{built ? <div className="generated-app"><aside className="generated-nav"><h3>Acme</h3><b>Dashboard</b><span>Customers</span><span>Workflows</span><span>Audit</span><span>Settings</span></aside><main className="generated-main"><div className="generated-head"><div><small>WORKSPACE</small><h1>{selected?.name ?? 'Generated application'}</h1><p>{prompt}</p></div><button>+ Add customer</button></div><div className="stats"><div><b>{workspaceFiles.length}</b><span>Generated files</span></div><div><b>{versions.length}</b><span>Versions</span></div><div><b>Ready</b><span>Build status</span></div></div><div className="table-card"><div className="table-title"><b>Generated workspace</b><span>View Code →</span></div>{workspaceFiles.slice(0,4).map(file=><div className="customer-row" key={file.path}><b>{file.path}</b><span>Generated</span><em>Ready</em></div>)}</div></main>{inspect&&<aside className="inspector"><h3>Inspector</h3><label>Selected</label><b>Primary button</b><label>Text</label><input value="+ Add customer" readOnly/><label>Radius</label><input value="8 px" readOnly/><label>Padding</label><input value="12 × 16" readOnly/><button onClick={()=>setInspect(false)}>Done</button></aside>}</div>:<div className="empty-preview"><div className="preview-icon">✦</div><h2>Ready to build</h2><p>Run Build to generate project files, preview state and a recoverable version.</p></div>}</div></div>
  }

  return <div className="app-shell"><header className="topbar"><div className="brand">ForgePilot <span>AI</span></div><div className="project-chip">{selected?.name ?? 'No project selected'}</div><div className="top-actions"><button>GitHub</button><button>Share</button><button className="publish">Publish</button></div></header><aside className="sidebar"><button className="new-project" onClick={()=>void createProject()} disabled={busy}>+ New project</button><div className="section-title">Projects</div><div className="project-list">{projects.map(project=><button key={project.id} className={selected?.id===project.id?'project active':'project'} onClick={()=>setSelected(project)}><strong>{project.name}</strong><span>{project.status} · {project.stack}</span></button>)}</div><div className="section-title">Workspace</div><div className="side-links"><button>Knowledge</button><button>Skills</button><button>Integrations</button><button>Members</button><button>Settings</button></div></aside><main className="workspace"><section className="chat-panel"><div className="panel-header"><span>AI Builder</span><span className="agent-pill">Agent</span></div><div className="conversation"><div className="welcome-card"><span className="eyebrow">BUILD WITH NATURAL LANGUAGE</span><h1>What should ForgePilot build?</h1><p>Plan, generate, edit and validate complete applications from one conversation.</p></div><div className="activity">{activity.map((line,index)=><div className="activity-row" key={`${line}-${index}`}>{line}</div>)}</div></div><div className="composer"><textarea value={prompt} onChange={event=>setPrompt(event.target.value)} rows={4}/><div className="composer-actions"><button className="secondary" onClick={()=>void run('plan')} disabled={!selected||busy}>Plan</button><button className="primary" onClick={()=>void run('build')} disabled={!selected||busy}>{busy?'Working…':'Build'}</button></div></div></section><section className="preview-panel"><div className="preview-toolbar"><div>{(['Preview','Code','Database','Versions','Security','Deploy'] as Tab[]).map(name=><button key={name} className={tab===name?'tab active':'tab'} onClick={()=>setTab(name)}>{name}</button>)}</div><div className="preview-status">● {selected?.status ?? 'Ready'}</div></div>{content()}</section></main></div>
}
