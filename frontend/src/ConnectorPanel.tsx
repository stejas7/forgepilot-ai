import {useEffect,useState} from 'react'

type Catalog={id:string;name:string;category:string;authType:string;fields:string[]}
type Connection={connectorId:string;name:string;status:string;config:Record<string,string>}

export default function ConnectorPanel({projectId}:{projectId:string}){
  const [catalog,setCatalog]=useState<Catalog[]>([]),[connections,setConnections]=useState<Connection[]>([]),[selected,setSelected]=useState<Catalog|null>(null),[config,setConfig]=useState<Record<string,string>>({}),[busy,setBusy]=useState('')
  useEffect(()=>{void load()},[projectId])
  async function load(){const [c,l]=await Promise.all([fetch('/api/connectors/catalog'),fetch(`/api/projects/${projectId}/connectors`)]);if(c.ok)setCatalog(await c.json());if(l.ok)setConnections(await l.json())}
  async function connect(){if(!selected)return;setBusy(selected.id);try{const r=await fetch(`/api/projects/${projectId}/connectors/${selected.id}`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(config)});if(r.ok){setSelected(null);setConfig({});await load()}}finally{setBusy('')}}
  async function health(id:string){setBusy(id);try{await fetch(`/api/projects/${projectId}/connectors/${id}/health`,{method:'POST'});await load()}finally{setBusy('')}}
  async function disconnect(id:string){setBusy(id);try{await fetch(`/api/projects/${projectId}/connectors/${id}`,{method:'DELETE'});await load()}finally{setBusy('')}}
  const connected=(id:string)=>connections.find(c=>c.connectorId===id)
  return <div className="p8-panel"><header><div><small>INTEGRATIONS</small><h2>Connectors</h2><p>Add payments, email, APIs and external services to this project.</p></div></header><div className="p8-grid">{catalog.map(item=>{const active=connected(item.id);return <div className="p8-card" key={item.id}><div><b>{item.name}</b><span>{item.category} · {item.authType}</span></div>{active?<><em>{active.status}</em><div className="p8-actions"><button disabled={!!busy} onClick={()=>void health(item.id)}>Check</button><button disabled={!!busy} onClick={()=>void disconnect(item.id)}>Disconnect</button></div></>:<button onClick={()=>{setSelected(item);setConfig({})}}>Connect</button>}</div>})}</div>{selected&&<div className="p8-config"><div className="p8-config-head"><div><b>Connect {selected.name}</b><span>Secrets are masked after save.</span></div><button onClick={()=>setSelected(null)}>×</button></div>{selected.fields.map(field=><label key={field}>{field}<input type={/key|secret|token|password/i.test(field)?'password':'text'} value={config[field]||''} onChange={e=>setConfig(current=>({...current,[field]:e.target.value}))}/></label>)}<button className="primary" disabled={busy===selected.id} onClick={()=>void connect()}>{busy===selected.id?'Connecting…':'Save connection'}</button></div>}</div>
}
