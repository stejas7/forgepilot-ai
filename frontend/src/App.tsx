import { FormEvent, useEffect, useState } from 'react'

type Project = {
  id: string
  name: string
  description: string
  stack: string
  status: string
}

type AgentResponse = {
  mode: string
  steps: string[]
  message: string
}

const initialPrompt = 'Build a customer onboarding application with authentication, dashboard, workflow and audit history.'

export default function App() {
  const [projects, setProjects] = useState<Project[]>([])
  const [selected, setSelected] = useState<Project | null>(null)
  const [prompt, setPrompt] = useState(initialPrompt)
  const [activity, setActivity] = useState<string[]>(['Create or select a project to begin.'])
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    void refreshProjects()
  }, [])

  async function refreshProjects() {
    const response = await fetch('/api/projects')
    if (response.ok) {
      const data: Project[] = await response.json()
      setProjects(data)
      if (!selected && data.length > 0) setSelected(data[0])
    }
  }

  async function createProject(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    try {
      const response = await fetch('/api/projects', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: `Untitled app ${projects.length + 1}`,
          description: 'Created from ForgePilot AI',
          stack: 'React + Spring Boot + PostgreSQL'
        })
      })
      if (!response.ok) throw new Error('Unable to create project')
      const project: Project = await response.json()
      setSelected(project)
      setActivity([`Project ${project.name} created.`])
      await refreshProjects()
    } finally {
      setBusy(false)
    }
  }

  async function run(mode: 'plan' | 'build') {
    if (!selected || !prompt.trim()) return
    setBusy(true)
    try {
      const response = await fetch(`/api/ai/${mode}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ projectId: selected.id, prompt })
      })
      if (!response.ok) throw new Error(`${mode} command failed`)
      const result: AgentResponse = await response.json()
      setActivity([result.message, ...result.steps])
      await refreshProjects()
    } catch (error) {
      setActivity([error instanceof Error ? error.message : 'Command failed'])
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">ForgePilot <span>AI</span></div>
        <div className="project-chip">{selected?.name ?? 'No project selected'}</div>
        <div className="top-actions"><button>GitHub</button><button>Share</button></div>
      </header>

      <aside className="sidebar">
        <button className="new-project" onClick={createProject} disabled={busy}>+ New project</button>
        <div className="section-title">Projects</div>
        <div className="project-list">
          {projects.map(project => (
            <button
              key={project.id}
              className={selected?.id === project.id ? 'project active' : 'project'}
              onClick={() => setSelected(project)}
            >
              <strong>{project.name}</strong>
              <span>{project.status} · {project.stack}</span>
            </button>
          ))}
        </div>
      </aside>

      <main className="workspace">
        <section className="chat-panel">
          <div className="panel-header">AI Builder</div>
          <div className="conversation">
            <div className="welcome-card">
              <span className="eyebrow">BUILD WITH NATURAL LANGUAGE</span>
              <h1>What should ForgePilot build?</h1>
              <p>Describe the product. Plan mode reasons first; Build mode will execute verified workspace changes as the agent runtime is completed.</p>
            </div>
            <div className="activity">
              {activity.map((line, index) => <div className="activity-row" key={`${line}-${index}`}>{line}</div>)}
            </div>
          </div>
          <div className="composer">
            <textarea value={prompt} onChange={event => setPrompt(event.target.value)} rows={4} />
            <div className="composer-actions">
              <button className="secondary" onClick={() => run('plan')} disabled={!selected || busy}>Plan</button>
              <button className="primary" onClick={() => run('build')} disabled={!selected || busy}>Build</button>
            </div>
          </div>
        </section>

        <section className="preview-panel">
          <div className="preview-toolbar">
            <div><button className="tab active">Preview</button><button className="tab">Code</button></div>
            <div className="preview-status">● Ready</div>
          </div>
          <div className="preview-canvas">
            <div className="empty-preview">
              <div className="preview-icon">⌘</div>
              <h2>Application preview</h2>
              <p>The isolated sandbox and live-rendering runtime arrives in the next milestone.</p>
              <div className="feature-grid">
                <div><strong>Plan</strong><span>Reason before changes</span></div>
                <div><strong>Build</strong><span>Agent execution contract</span></div>
                <div><strong>Code</strong><span>Workspace editor next</span></div>
                <div><strong>Preview</strong><span>Sandbox runtime next</span></div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}
