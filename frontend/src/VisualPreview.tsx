import { useEffect, useRef, useState } from 'react'

type Viewport='desktop'|'tablet'|'mobile'
type Selection={tag:string;selector:string;text:string;color:string;backgroundColor:string;fontSize:string;fontWeight:string;textAlign:string;padding:string;margin:string;borderRadius:string;width:string}
type RuntimeReport={status:string;ready:boolean;checkedAt:string;issues:string[];logs:string[]}
type WorkspaceVersion={id:string;label:string;createdAt:string;files:Record<string,string>}
type Props={projectId:string;projectName:string;refresh:number;setRefresh:(update:(value:number)=>number)=>void;viewport:Viewport;onTargetPrompt?:(text:string)=>void}

export default function VisualPreview({projectId,projectName,refresh,setRefresh,viewport,onTargetPrompt}:Props){
  const frameRef=useRef<HTMLIFrameElement|null>(null),selectedRef=useRef<HTMLElement|null>(null),undoRef=useRef<string[]>([]),redoRef=useRef<string[]>([])
  const [inspectMode,setInspectMode]=useState(false),[selection,setSelection]=useState<Selection|null>(null),[saving,setSaving]=useState(false),[historyTick,setHistoryTick]=useState(0)
  const [runtime,setRuntime]=useState<RuntimeReport|null>(null),[runtimeOpen,setRuntimeOpen]=useState(false),[repairing,setRepairing]=useState(false),[restarting,setRestarting]=useState(false)
  const [versions,setVersions]=useState<WorkspaceVersion[]>([]),[versionsOpen,setVersionsOpen]=useState(false),[restoring,setRestoring]=useState(''),[responsiveOnly,setResponsiveOnly]=useState(false)

  useEffect(()=>{if(!inspectMode)clearSelection()},[inspectMode])
  useEffect(()=>{void verifyRuntime();void loadVersions()},[projectId,refresh])

  async function verifyRuntime(){const r=await fetch(`/api/projects/${projectId}/runtime`);if(r.ok)setRuntime(await r.json())}
  async function restartRuntime(){setRestarting(true);try{const r=await fetch(`/api/projects/${projectId}/runtime/restart`,{method:'POST'});if(r.ok){setRuntime(await r.json());setRefresh(v=>v+1)}}finally{setRestarting(false)}}
  async function repairRuntime(){setRepairing(true);try{const r=await fetch(`/api/projects/${projectId}/runtime/repair`,{method:'POST'});if(r.ok){setRuntime(await r.json());setRefresh(v=>v+1);await loadVersions()}}finally{setRepairing(false)}}
  async function loadVersions(){const r=await fetch(`/api/projects/${projectId}/workspace/versions`);if(r.ok)setVersions(await r.json())}
  async function restoreVersion(id:string){setRestoring(id);try{const r=await fetch(`/api/projects/${projectId}/workspace/versions/${id}/restore`,{method:'POST'});if(!r.ok)throw new Error('Restore failed');setVersionsOpen(false);clearSelection();setRefresh(v=>v+1)}finally{setRestoring('')}}

  function onFrameLoad(){const doc=frameRef.current?.contentDocument;if(!doc)return;ensureInspectorStyle(doc);doc.addEventListener('click',handlePreviewClick,true);void verifyRuntime()}
  function ensureInspectorStyle(doc:Document){if(doc.getElementById('forgepilot-inspector-style'))return;const s=doc.createElement('style');s.id='forgepilot-inspector-style';s.textContent='.forgepilot-selected{outline:2px solid #7c3aed!important;outline-offset:2px!important;cursor:crosshair!important}';doc.head.appendChild(s)}
  function handlePreviewClick(event:Event){if(!inspectMode)return;event.preventDefault();event.stopPropagation();const target=event.target;const FrameHTMLElement=frameRef.current?.contentWindow?.HTMLElement;if(!FrameHTMLElement||!(target instanceof FrameHTMLElement))return;selectElement(target as HTMLElement)}
  function stableSelector(element:HTMLElement){let id=element.dataset.forgepilotId;if(!id){id=`fp-${Date.now().toString(36)}-${Math.random().toString(36).slice(2,7)}`;element.dataset.forgepilotId=id}return `[data-forgepilot-id="${id}"]`}
  function selectElement(element:HTMLElement){selectedRef.current?.classList.remove('forgepilot-selected');selectedRef.current=element;element.classList.add('forgepilot-selected');syncSelection(element)}
  function syncSelection(element:HTMLElement){const style=element.ownerDocument.defaultView?.getComputedStyle(element);setSelection({tag:element.tagName.toLowerCase(),selector:stableSelector(element),text:element.innerText||'',color:style?.color||'',backgroundColor:style?.backgroundColor||'',fontSize:style?.fontSize||'',fontWeight:style?.fontWeight||'',textAlign:style?.textAlign||'',padding:style?.padding||'',margin:style?.margin||'',borderRadius:style?.borderRadius||'',width:style?.width||''})}
  function clearSelection(){selectedRef.current?.classList.remove('forgepilot-selected');selectedRef.current=null;setSelection(null)}
  function cleanHtml(){const doc=frameRef.current?.contentDocument;if(!doc)return'';const clone=doc.documentElement.cloneNode(true) as HTMLElement;clone.querySelectorAll('.forgepilot-selected').forEach(n=>n.classList.remove('forgepilot-selected'));clone.querySelector('#forgepilot-inspector-style')?.remove();return'<!doctype html>\n'+clone.outerHTML}
  function pushUndo(){const html=cleanHtml();if(!html)return;undoRef.current=[...undoRef.current.slice(-19),html];redoRef.current=[];setHistoryTick(v=>v+1)}

  function cssName(field:keyof Selection){return({color:'color',backgroundColor:'background-color',fontSize:'font-size',fontWeight:'font-weight',textAlign:'text-align',padding:'padding',margin:'margin',borderRadius:'border-radius',width:'width'} as Record<string,string>)[field]}
  function applyResponsive(field:keyof Selection,value:string){const doc=frameRef.current?.contentDocument;if(!doc||!selection)return;let style=doc.getElementById('forgepilot-responsive-overrides') as HTMLStyleElement|null;if(!style){style=doc.createElement('style');style.id='forgepilot-responsive-overrides';doc.head.appendChild(style)}const max=viewport==='mobile'?'480px':'900px';const prop=cssName(field);if(!prop)return;style.textContent+=`\n@media (max-width:${max}){${selection.selector}{${prop}:${value}!important;}}`}
  function applyField(field:keyof Selection,value:string){const el=selectedRef.current;if(!el||!selection||field==='selector'||field==='tag')return;pushUndo();setSelection({...selection,[field]:value});if(field==='text'){el.innerText=value;return}if(responsiveOnly&&viewport!=='desktop'){applyResponsive(field,value);return}if(field==='color')el.style.color=value;if(field==='backgroundColor')el.style.backgroundColor=value;if(field==='fontSize')el.style.fontSize=value;if(field==='fontWeight')el.style.fontWeight=value;if(field==='textAlign')el.style.textAlign=value;if(field==='padding')el.style.padding=value;if(field==='margin')el.style.margin=value;if(field==='borderRadius')el.style.borderRadius=value;if(field==='width')el.style.width=value}

  function replaceDocument(html:string){const doc=frameRef.current?.contentDocument;if(!doc)return;doc.open();doc.write(html);doc.close();ensureInspectorStyle(doc);doc.addEventListener('click',handlePreviewClick,true);clearSelection()}
  function undo(){const prev=undoRef.current.pop();if(!prev)return;const cur=cleanHtml();if(cur)redoRef.current.push(cur);replaceDocument(prev);setHistoryTick(v=>v+1)}
  function redo(){const next=redoRef.current.pop();if(!next)return;const cur=cleanHtml();if(cur)undoRef.current.push(cur);replaceDocument(next);setHistoryTick(v=>v+1)}
  async function saveVisualChanges(){const html=cleanHtml();if(!html)return;setSaving(true);try{const r=await fetch(`/api/projects/${projectId}/workspace/files/${encodeURI('preview/index.html')}`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({content:html})});if(!r.ok)throw new Error('Unable to save visual changes');await fetch(`/api/projects/${projectId}/workspace/versions`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({label:`Visual edit: ${selection?.selector||'preview'}`})});undoRef.current=[];redoRef.current=[];setHistoryTick(v=>v+1);clearSelection();setInspectMode(false);setRefresh(v=>v+1);await loadVersions()}finally{setSaving(false)}}
  function sendTargetToAI(){
    if(!selection)return
    const text=`Update the selected component ${selection.selector} (<${selection.tag}>). Current text: "${selection.text.slice(0,180)}". `
    if(onTargetPrompt){onTargetPrompt(text);return}
    const composer=document.querySelector('.builder-composer textarea') as HTMLTextAreaElement|null
    if(!composer)return
    const setter=Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype,'value')?.set
    setter?.call(composer,text)
    composer.dispatchEvent(new Event('input',{bubbles:true}))
    composer.focus()
  }

  const previewUrl=`/api/projects/${projectId}/preview?v=${refresh}`
  return <div className="visual-workspace">
    <div className="visual-toolbar"><button className={inspectMode?'active':''} onClick={()=>setInspectMode(v=>!v)}>⌖ Select</button><button disabled={!undoRef.current.length} onClick={undo}>↶ Undo</button><button disabled={!redoRef.current.length} onClick={redo}>↷ Redo</button><button className={runtime?.ready?'runtime-ready':'runtime-error'} onClick={()=>setRuntimeOpen(v=>!v)}>● {runtime?.status||'Checking'}</button><button onClick={()=>setVersionsOpen(v=>!v)}>◷ Versions {versions.length?`(${versions.length})`:''}</button><span>{inspectMode?'Click any element in the preview':'Preview mode'}{historyTick>0?'':''}</span>{selection&&<button className="visual-save" disabled={saving} onClick={()=>void saveVisualChanges()}>{saving?'Saving…':'Save visual edit'}</button>}</div>
    {runtimeOpen&&<div className="runtime-drawer"><div><b>Runtime verification</b><span>{runtime?.checkedAt?new Date(runtime.checkedAt).toLocaleTimeString():''}</span></div>{runtime?.logs.map((log,i)=><p key={i}>✓ {log}</p>)}{runtime?.issues.map((issue,i)=><p className="runtime-issue" key={i}>! {issue}</p>)}<div className="runtime-actions"><button disabled={restarting} onClick={()=>void restartRuntime()}>{restarting?'Restarting…':'Restart preview'}</button>{runtime&&!runtime.ready&&<button disabled={repairing} onClick={()=>void repairRuntime()}>{repairing?'Repairing…':'Auto repair preview'}</button>}</div></div>}
    {runtime&&!runtime.ready&&<div className="preview-error-overlay"><b>Preview needs attention</b><span>{runtime.issues[0]||'Generated application failed verification.'}</span><div><button onClick={()=>void restartRuntime()}>Restart</button><button onClick={()=>void repairRuntime()}>Auto repair</button></div></div>}
    {versionsOpen&&<div className="version-drawer"><div className="drawer-head"><b>Version history</b><button onClick={()=>setVersionsOpen(false)}>×</button></div>{versions.map(v=><div className="version-row" key={v.id}><div><b>{v.label}</b><span>{new Date(v.createdAt).toLocaleString()}</span></div><button disabled={restoring===v.id} onClick={()=>void restoreVersion(v.id)}>{restoring===v.id?'Restoring…':'Restore'}</button></div>)}</div>}
    <div className="visual-body"><div className="preview-stage"><div className={`preview-browser ${viewport}`}><iframe ref={frameRef} key={refresh} title={`${projectName} preview`} src={previewUrl} sandbox="allow-scripts allow-forms allow-modals allow-same-origin" onLoad={onFrameLoad}/></div></div>{selection&&<aside className="visual-inspector">
      <div className="inspector-head"><div><small>SELECTED ELEMENT</small><b>&lt;{selection.tag}&gt;</b></div><button onClick={clearSelection}>×</button></div>
      <div className="target-box"><small>Stable target</small><code>{selection.selector}</code><button onClick={sendTargetToAI}>Ask AI about selection</button></div>
      {viewport!=='desktop'&&<label className="responsive-toggle"><input type="checkbox" checked={responsiveOnly} onChange={e=>setResponsiveOnly(e.target.checked)}/> Apply styles only to {viewport}</label>}
      <label>Text<textarea value={selection.text} onChange={e=>applyField('text',e.target.value)}/></label><div className="inspector-grid"><label>Font size<input value={selection.fontSize} onChange={e=>applyField('fontSize',e.target.value)}/></label><label>Weight<input value={selection.fontWeight} onChange={e=>applyField('fontWeight',e.target.value)}/></label></div><label>Alignment<select value={selection.textAlign} onChange={e=>applyField('textAlign',e.target.value)}><option value="left">Left</option><option value="center">Center</option><option value="right">Right</option><option value="justify">Justify</option></select></label><label>Text color<input value={selection.color} onChange={e=>applyField('color',e.target.value)}/></label><label>Background<input value={selection.backgroundColor} onChange={e=>applyField('backgroundColor',e.target.value)}/></label><div className="inspector-grid"><label>Padding<input value={selection.padding} onChange={e=>applyField('padding',e.target.value)}/></label><label>Margin<input value={selection.margin} onChange={e=>applyField('margin',e.target.value)}/></label></div><div className="inspector-grid"><label>Width<input value={selection.width} onChange={e=>applyField('width',e.target.value)}/></label><label>Radius<input value={selection.borderRadius} onChange={e=>applyField('borderRadius',e.target.value)}/></label></div><div className="inspector-note">Stable ForgePilot IDs persist with the saved preview. Responsive overrides are stored in the project document and every save creates a recoverable version.</div>
    </aside>}</div>
  </div>
}
