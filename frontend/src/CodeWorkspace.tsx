import {useEffect,useMemo,useState} from 'react'

type WorkspaceFile={path:string;content:string}
type WorkspaceVersion={id:string;label:string;createdAt:string;files:Record<string,string>}
type VersionDiff={versionId:string;label:string;createdAt:string;files:{path:string;status:string;before:string|null;after:string|null}[]}

type Props={projectId:string;refreshToken:number;onPreviewChanged:()=>void}

export default function CodeWorkspace({projectId,refreshToken,onPreviewChanged}:Props){
  const [files,setFiles]=useState<WorkspaceFile[]>([]),[selected,setSelected]=useState(''),[draft,setDraft]=useState(''),[dirty,setDirty]=useState(false)
  const [query,setQuery]=useState(''),[saving,setSaving]=useState(false),[versions,setVersions]=useState<WorkspaceVersion[]>([]),[historyOpen,setHistoryOpen]=useState(false)
  const [diff,setDiff]=useState<VersionDiff|null>(null),[newPath,setNewPath]=useState('')
  useEffect(()=>{void load()},[projectId,refreshToken])
  useEffect(()=>{const file=files.find(f=>f.path===selected);setDraft(file?.content??'');setDirty(false)},[selected,files])

  async function load(){
    const [fileResponse,versionResponse]=await Promise.all([fetch(`/api/projects/${projectId}/workspace/files`),fetch(`/api/projects/${projectId}/workspace/versions`)])
    if(fileResponse.ok){const data:WorkspaceFile[]=await fileResponse.json();setFiles(data);if(data.length&&!data.some(f=>f.path===selected))setSelected(data[0].path)}
    if(versionResponse.ok)setVersions(await versionResponse.json())
  }
  async function snapshot(label:string){await fetch(`/api/projects/${projectId}/workspace/versions`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({label})});await load()}
  async function save(){if(!selected||!dirty)return;setSaving(true);try{const r=await fetch(`/api/projects/${projectId}/workspace/files/${encodeURI(selected)}`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({content:draft})});if(!r.ok)throw new Error('Save failed');await snapshot(`Manual edit: ${selected}`);setDirty(false);if(selected==='preview/index.html')onPreviewChanged()}finally{setSaving(false)}}
  async function createFile(){const path=newPath.trim();if(!path)return;const r=await fetch(`/api/projects/${projectId}/workspace/files`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({path,content:''})});if(!r.ok)return;await snapshot(`Create file: ${path}`);setNewPath('');await load();setSelected(path)}
  async function renameFile(){if(!selected)return;const to=window.prompt('Rename file',selected)?.trim();if(!to||to===selected)return;const r=await fetch(`/api/projects/${projectId}/workspace/files/rename`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({from:selected,to})});if(!r.ok)return;await snapshot(`Rename file: ${selected} → ${to}`);setSelected(to);await load();if(selected==='preview/index.html'||to==='preview/index.html')onPreviewChanged()}
  async function deleteFile(){if(!selected||!window.confirm(`Delete ${selected}?`))return;const current=selected;const r=await fetch(`/api/projects/${projectId}/workspace/files/${encodeURI(current)}`,{method:'DELETE'});if(!r.ok)return;await snapshot(`Delete file: ${current}`);setSelected('');await load();if(current==='preview/index.html')onPreviewChanged()}
  async function restore(versionId:string){const r=await fetch(`/api/projects/${projectId}/workspace/versions/${versionId}/restore`,{method:'POST'});if(!r.ok)return;setDiff(null);await load();onPreviewChanged()}
  async function showDiff(versionId:string){const r=await fetch(`/api/projects/${projectId}/workspace/versions/${versionId}/diff`);if(r.ok)setDiff(await r.json())}
  const visible=useMemo(()=>files.filter(f=>`${f.path} ${f.content}`.toLowerCase().includes(query.toLowerCase())),[files,query])

  return <div className="p5-workspace">
    <aside className="p5-files"><div className="p5-search"><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search files…"/></div><div className="p5-create"><input value={newPath} onChange={e=>setNewPath(e.target.value)} placeholder="new/file.ts"/><button onClick={()=>void createFile()}>＋</button></div><div className="p5-file-list">{visible.map(file=><button key={file.path} className={selected===file.path?'active':''} onClick={()=>setSelected(file.path)}>{file.path}</button>)}</div><button className="p5-history-button" onClick={()=>setHistoryOpen(v=>!v)}>◷ History {versions.length?`(${versions.length})`:''}</button></aside>
    <section className="p5-editor"><div className="p5-editor-toolbar"><span>{selected||'No file selected'}{dirty?' • modified':''}</span><div><button disabled={!selected} onClick={renameFile}>Rename</button><button disabled={!selected} onClick={deleteFile}>Delete</button><button disabled={!dirty||saving} onClick={()=>void save()}>{saving?'Saving…':'Save'}</button></div></div><textarea value={draft} onChange={e=>{setDraft(e.target.value);setDirty(true)}} spellCheck={false}/></section>
    {historyOpen&&<aside className="p5-history"><div className="p5-history-head"><b>Version history</b><button onClick={()=>setHistoryOpen(false)}>×</button></div>{versions.map(version=><div className="p5-version" key={version.id}><div><b>{version.label}</b><span>{new Date(version.createdAt).toLocaleString()}</span></div><div><button onClick={()=>void showDiff(version.id)}>Diff</button><button onClick={()=>void restore(version.id)}>Restore</button></div></div>)}</aside>}
    {diff&&<div className="p5-diff"><div className="p5-history-head"><b>Changes since {diff.label}</b><button onClick={()=>setDiff(null)}>×</button></div>{diff.files.length===0?<p>No changes.</p>:diff.files.map(file=><div key={file.path} className="p5-diff-file"><b>{file.status} · {file.path}</b><div className="p5-diff-columns"><pre>{file.before??'∅'}</pre><pre>{file.after??'∅'}</pre></div></div>)}</div>}
  </div>
}
