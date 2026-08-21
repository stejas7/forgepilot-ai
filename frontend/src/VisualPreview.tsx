import { useEffect, useRef, useState } from 'react'

type Viewport = 'desktop' | 'tablet' | 'mobile'
type Selection = {
  tag: string
  text: string
  color: string
  backgroundColor: string
  fontSize: string
  fontWeight: string
  textAlign: string
  padding: string
  margin: string
  borderRadius: string
  width: string
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
  const undoRef = useRef<string[]>([])
  const redoRef = useRef<string[]>([])
  const [inspectMode,setInspectMode]=useState(false)
  const [selection,setSelection]=useState<Selection|null>(null)
  const [saving,setSaving]=useState(false)
  const [historyTick,setHistoryTick]=useState(0)

  useEffect(()=>{ if(!inspectMode) clearSelection() },[inspectMode])

  function onFrameLoad(){
    const doc=frameRef.current?.contentDocument
    if(!doc)return
    ensureInspectorStyle(doc)
    doc.addEventListener('click',handlePreviewClick,true)
  }

  function ensureInspectorStyle(doc:Document){
    if(doc.getElementById('forgepilot-inspector-style'))return
    const style=doc.createElement('style')
    style.id='forgepilot-inspector-style'
    style.textContent='.forgepilot-selected{outline:2px solid #7c3aed!important;outline-offset:2px!important;cursor:crosshair!important}'
    doc.head.appendChild(style)
  }

  function handlePreviewClick(event:Event){
    if(!inspectMode)return
    event.preventDefault()
    event.stopPropagation()
    const target=event.target
    const FrameHTMLElement=frameRef.current?.contentWindow?.HTMLElement
    if(!FrameHTMLElement||!(target instanceof FrameHTMLElement))return
    selectElement(target as HTMLElement)
  }

  function selectElement(element:HTMLElement){
    selectedRef.current?.classList.remove('forgepilot-selected')
    selectedRef.current=element
    element.classList.add('forgepilot-selected')
    syncSelection(element)
  }

  function syncSelection(element:HTMLElement){
    const style=element.ownerDocument.defaultView?.getComputedStyle(element)
    setSelection({
      tag:element.tagName.toLowerCase(),
      text:element.innerText||'',
      color:style?.color||'',
      backgroundColor:style?.backgroundColor||'',
      fontSize:style?.fontSize||'',
      fontWeight:style?.fontWeight||'',
      textAlign:style?.textAlign||'',
      padding:style?.padding||'',
      margin:style?.margin||'',
      borderRadius:style?.borderRadius||'',
      width:style?.width||''
    })
  }

  function clearSelection(){
    selectedRef.current?.classList.remove('forgepilot-selected')
    selectedRef.current=null
    setSelection(null)
  }

  function cleanHtml(){
    const doc=frameRef.current?.contentDocument
    if(!doc)return ''
    const clone=doc.documentElement.cloneNode(true) as HTMLElement
    clone.querySelectorAll('.forgepilot-selected').forEach(node=>node.classList.remove('forgepilot-selected'))
    clone.querySelector('#forgepilot-inspector-style')?.remove()
    return '<!doctype html>\n'+clone.outerHTML
  }

  function pushUndo(){
    const html=cleanHtml()
    if(!html)return
    undoRef.current=[...undoRef.current.slice(-19),html]
    redoRef.current=[]
    setHistoryTick(value=>value+1)
  }

  function applyField(field:keyof Selection,value:string){
    const element=selectedRef.current
    if(!element||!selection)return
    pushUndo()
    setSelection({...selection,[field]:value})
    if(field==='text') element.innerText=value
    if(field==='color') element.style.color=value
    if(field==='backgroundColor') element.style.backgroundColor=value
    if(field==='fontSize') element.style.fontSize=value
    if(field==='fontWeight') element.style.fontWeight=value
    if(field==='textAlign') element.style.textAlign=value
    if(field==='padding') element.style.padding=value
    if(field==='margin') element.style.margin=value
    if(field==='borderRadius') element.style.borderRadius=value
    if(field==='width') element.style.width=value
  }

  function replaceDocument(html:string){
    const doc=frameRef.current?.contentDocument
    if(!doc)return
    doc.open();doc.write(html);doc.close()
    ensureInspectorStyle(doc)
    doc.addEventListener('click',handlePreviewClick,true)
    clearSelection()
  }

  function undo(){
    const previous=undoRef.current.pop()
    if(!previous)return
    const current=cleanHtml()
    if(current)redoRef.current.push(current)
    replaceDocument(previous)
    setHistoryTick(value=>value+1)
  }

  function redo(){
    const next=redoRef.current.pop()
    if(!next)return
    const current=cleanHtml()
    if(current)undoRef.current.push(current)
    replaceDocument(next)
    setHistoryTick(value=>value+1)
  }

  async function saveVisualChanges(){
    const html=cleanHtml()
    if(!html)return
    setSaving(true)
    try{
      const response=await fetch(`/api/projects/${projectId}/workspace/files/${encodeURI('preview/index.html')}`,{
        method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({content:html})
      })
      if(!response.ok)throw new Error('Unable to save visual changes')
      await fetch(`/api/projects/${projectId}/workspace/versions`,{
        method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({label:'Visual edit'})
      })
      undoRef.current=[];redoRef.current=[];setHistoryTick(value=>value+1)
      clearSelection();setInspectMode(false);setRefresh(value=>value+1)
    }finally{setSaving(false)}
  }

  const previewUrl=`/api/projects/${projectId}/preview?v=${refresh}`

  return <div className="visual-workspace">
    <div className="visual-toolbar">
      <button className={inspectMode?'active':''} onClick={()=>setInspectMode(value=>!value)}>⌖ Select</button>
      <button disabled={undoRef.current.length===0} onClick={undo}>↶ Undo</button>
      <button disabled={redoRef.current.length===0} onClick={redo}>↷ Redo</button>
      <span>{inspectMode?'Click any element in the preview':'Preview mode'}{historyTick>0?'':''}</span>
      {selection&&<button className="visual-save" disabled={saving} onClick={()=>void saveVisualChanges()}>{saving?'Saving…':'Save visual edit'}</button>}
    </div>
    <div className="visual-body">
      <div className="preview-stage"><div className={`preview-browser ${viewport}`}><iframe ref={frameRef} key={refresh} title={`${projectName} preview`} src={previewUrl} onLoad={onFrameLoad}/></div></div>
      {selection&&<aside className="visual-inspector">
        <div className="inspector-head"><div><small>SELECTED ELEMENT</small><b>&lt;{selection.tag}&gt;</b></div><button onClick={clearSelection}>×</button></div>
        <label>Text<textarea value={selection.text} onChange={e=>applyField('text',e.target.value)}/></label>
        <div className="inspector-grid"><label>Font size<input value={selection.fontSize} onChange={e=>applyField('fontSize',e.target.value)}/></label><label>Weight<input value={selection.fontWeight} onChange={e=>applyField('fontWeight',e.target.value)}/></label></div>
        <label>Alignment<select value={selection.textAlign} onChange={e=>applyField('textAlign',e.target.value)}><option value="left">Left</option><option value="center">Center</option><option value="right">Right</option><option value="justify">Justify</option></select></label>
        <label>Text color<input value={selection.color} onChange={e=>applyField('color',e.target.value)}/></label>
        <label>Background<input value={selection.backgroundColor} onChange={e=>applyField('backgroundColor',e.target.value)}/></label>
        <div className="inspector-grid"><label>Padding<input value={selection.padding} onChange={e=>applyField('padding',e.target.value)}/></label><label>Margin<input value={selection.margin} onChange={e=>applyField('margin',e.target.value)}/></label></div>
        <div className="inspector-grid"><label>Width<input value={selection.width} onChange={e=>applyField('width',e.target.value)}/></label><label>Radius<input value={selection.borderRadius} onChange={e=>applyField('borderRadius',e.target.value)}/></label></div>
        <div className="inspector-note">Visual changes are live. Undo/redo stays local until Save creates a recoverable ForgePilot version.</div>
      </aside>}
    </div>
  </div>
}
