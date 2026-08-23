import {useEffect,useState} from 'react'

type StoredObject={key:string;contentType:string;size:number;updatedAt:string}

export default function ObjectStoragePanel({projectId}:{projectId:string}){
  const [objects,setObjects]=useState<StoredObject[]>([]),[busy,setBusy]=useState(false),[error,setError]=useState('')
  useEffect(()=>{void load()},[projectId])
  async function load(){const r=await fetch(`/api/projects/${projectId}/storage/objects`);if(r.ok)setObjects(await r.json())}
  async function upload(file:File){setBusy(true);setError('');try{const base64=await new Promise<string>((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(String(reader.result).split(',')[1]||'');reader.onerror=()=>reject(reader.error);reader.readAsDataURL(file)});const r=await fetch(`/api/projects/${projectId}/storage/objects`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({key:`uploads/${file.name}`,contentType:file.type||'application/octet-stream',base64})});if(!r.ok)throw new Error(await r.text()||'Upload failed');await load()}catch(e){setError(e instanceof Error?e.message:'Upload failed')}finally{setBusy(false)}}
  async function remove(key:string){setBusy(true);try{const r=await fetch(`/api/projects/${projectId}/storage/objects/${encodeURIComponent(key)}`,{method:'DELETE'});if(r.ok)await load()}finally{setBusy(false)}}
  async function scaffold(){setBusy(true);try{await fetch(`/api/projects/${projectId}/storage/scaffold`,{method:'POST'});await load()}finally{setBusy(false)}}
  return <section className="backend-wide"><div className="storage-head"><div><h3>File & object storage</h3><p>Upload project assets, keep metadata, and generate typed storage helpers for the generated app.</p></div><button disabled={busy} onClick={()=>void scaffold()}>Generate storage client</button></div><label className="storage-drop"><input type="file" disabled={busy} onChange={e=>{const file=e.target.files?.[0];if(file)void upload(file);e.currentTarget.value=''}}/><span>{busy?'Working…':'Choose file to upload'}</span><small>Project scoped · 5 MB per object</small></label>{error&&<p className="storage-error">{error}</p>}<div className="storage-list">{objects.length===0?<p>No stored objects yet.</p>:objects.map(object=><div className="storage-row" key={object.key}><div><b>{object.key}</b><span>{object.contentType} · {(object.size/1024).toFixed(1)} KB</span></div><button disabled={busy} onClick={()=>void remove(object.key)}>Delete</button></div>)}</div></section>
}
