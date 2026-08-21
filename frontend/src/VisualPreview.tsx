import { useEffect, useRef, useState } from 'react'

type Viewport = 'desktop' | 'tablet' | 'mobile'
type Selection = {
  tag: string
  text: string
  color: string
  backgroundColor: string
  fontSize: string
  padding: string
  borderRadius: string
}

type Props = {
  projectId: string
  projectName: string
  refresh: number
  setRefresh: (update: (value: number) => number) => void
  viewport: Viewport
}

export default function VisualPreview({projectId, projectName, refresh, setRefresh, viewport}: Props) {
  const frameRef = useRef<HTMLIFrameElement | null>(null)
  const selectedRef = useRef<HTMLElement | null>(null)
  const [inspectMode,setInspectMode]=useState(false)
  const [selection,setSelection]=useState<Selection|null>(null)
  const [saving,setSaving]=useState(false)

  useEffect(()=>{
    if(!inspectMode) clearSelection()
  },[inspectMode])

  function onFrameLoad(){
    const frame=frameRef.current
    const doc=frame?.contentDocument
    if(!doc)return
    ensureInspectorStyle(doc)
    doc.addEventListener('click',handlePreviewClick,true)
  }

  function ensureInspectorStyle(doc:Document){
    if(doc.getElementById('forgepilot-inspector-style'))return
    const style=doc.createElement('style')
    style.id='forgepilot-inspector-style'
    style.textContent='.forgepilot-selected{outline:2px solid #7c3aed!important;outline-offset:2px!important;cursor:crosshair!important}.forgepilot-inspectable:hover{outline:1px dashed #8b5cf6;outline-offset:1px}'
    doc.head.appendChild(style)
  }

  function handlePreviewClick(event:Event){
    if(!inspectMode)return
    event.preventDefault()
    event.stopPropagation()
    const target=event.target
    if(!(target instanceof HTMLElement))return
    selectElement(target)
  }

  function selectElement(element:HTMLElement){
    if(selectedRef.current)selectedRef.current.classList.remove('forgepilot-selected')
    selectedRef.current=element
    element.classList.add('forgepilot-selected')
    const style=element.ownerDocument.defaultView?.getComputedStyle(element)
    setSelection({
      tag:element.tagName.toLowerCase(),
      text:element.innerText||'',
      color:style?.color||'',
      backgroundColor:style?.backgroundColor||'',
      fontSize:style?.fontSize||'',
      padding:style?.padding||'',
      borderRadius:style?.borderRadius||''
    })
  }

  function clearSelection(){
    selectedRef.current?.classList.remove('forgepilot-selected')
    selectedRef.current=null
    setSelection(null)
  }

  function applyField(field:keyof Selection,value:string){
    const element=selectedRef.current
    if(!element||!selection)return
    setSelection({...selection,[field]:value})
    if(field==='text') element.innerText=value
    if(field==='color') element.style.color=value
    if(field==='backgroundColor') element.style.backgroundColor=value
    if(field==='fontSize') element.style.fontSize=value
    if(field==='padding') element.style.padding=value
    if(field==='borderRadius') element.style.borderRadius=value
  }

  async function saveVisualChanges(){
    const doc=frameRef.current?.contentDocument
    if(!doc)return
    setSaving(true)
    try{
      const clone=doc.documentElement.cloneNode(true) as HTMLElement
      clone.querySelectorAll('.forgepilot-selected').forEach(node=>node.classList.remove('forgepilot-selected'))
      clone.querySelector('#forgepilot-inspector-style')?.remove()
      const html='<!doctype html>\n'+clone.outerHTML
      const response=await fetch(`/api/projects/${projectId}/workspace/files/${encodeURI('preview/index.html')}`,{
        method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({content:html})
      })
      if(!response.ok)throw new Error('Unable to save visual changes')
      await fetch(`/api/projects/${projectId}/workspace/versions`,{
        method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({label:'Visual edit'})
      })
      clearSelection()
      setInspectMode(false)
      setRefresh(value=>value+1)
    }finally{setSaving(false)}
  }

  const previewUrl=`/api/projects/${projectId}/preview?v=${refresh}`

  return <div className="visual-workspace">
    <div className="visual-toolbar">
      <button className={inspectMode?'active':''} onClick={()=>setInspectMode(value=>!value)}>⌖ Select</button>
      <span>{inspectMode?'Click any element in the preview':'Preview mode'}</span>
      {selection&&<button className="visual-save" disabled={saving} onClick={()=>void saveVisualChanges()}>{saving?'Saving…':'Save visual edit'}</button>}
    </div>
    <div className="visual-body">
      <div className="preview-stage">
        <div className={`preview-browser ${viewport}`}>
          <iframe ref={frameRef} key={refresh} title={`${projectName} preview`} src={previewUrl} onLoad={onFrameLoad}/>
        </div>
      </div>
      {selection&&<aside className="visual-inspector">
        <div className="inspector-head"><div><small>SELECTED ELEMENT</small><b>&lt;{selection.tag}&gt;</b></div><button onClick={clearSelection}>×</button></div>
        <label>Text<textarea value={selection.text} onChange={e=>applyField('text',e.target.value)}/></label>
        <label>Text color<input value={selection.color} onChange={e=>applyField('color',e.target.value)}/></label>
        <label>Background<input value={selection.backgroundColor} onChange={e=>applyField('backgroundColor',e.target.value)}/></label>
        <label>Font size<input value={selection.fontSize} onChange={e=>applyField('fontSize',e.target.value)}/></label>
        <label>Padding<input value={selection.padding} onChange={e=>applyField('padding',e.target.value)}/></label>
        <label>Radius<input value={selection.borderRadius} onChange={e=>applyField('borderRadius',e.target.value)}/></label>
        <div className="inspector-note">Changes are applied live. Save creates a recoverable ForgePilot version.</div>
      </aside>}
    </div>
  </div>
}
