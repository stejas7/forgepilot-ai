import {useState} from 'react'

type Mode='build'|'plan'
type Props={prompt:string;mode:Mode;onComplete:()=>void;onCancel:()=>void}
type Project={id:string;name:string}

type StreamEvent={type:string;message?:string}

export default function PromptHandoff({prompt,mode,onComplete,onCancel}:Props){
  const [busy,setBusy]=useState(false),[steps,setSteps]=useState<string[]>([]),[error,setError]=useState('')
  async function run(){
    setBusy(true);setError('');setSteps(['Creating ForgePilot project…'])
    try{
      const created=await fetch('/api/projects',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:projectName(prompt),description:prompt,stack:'React + Spring Boot + PostgreSQL'})})
      if(!created.ok)throw new Error(`Project creation failed (${created.status})`)
      const project:Project=await created.json()
      setSteps(current=>[...current,`${mode==='plan'?'Planning':'Building'} with ForgePilot…`])
      const response=await fetch(`/api/ai/${mode}/stream`,{method:'POST',headers:{'Content-Type':'application/json','Accept':'application/x-ndjson'},body:JSON.stringify({projectId:project.id,prompt})})
      if(!response.ok||!response.body)throw new Error(`${mode==='plan'?'Plan':'Build'} request failed (${response.status})`)
      const reader=response.body.getReader(),decoder=new TextDecoder();let buffer=''
      while(true){
        const {value,done}=await reader.read();buffer+=decoder.decode(value??new Uint8Array(),{stream:!done});const lines=buffer.split('\n');buffer=lines.pop()??''
        for(const line of lines){if(!line.trim())continue;const event:StreamEvent=JSON.parse(line);if(event.message&&(event.type==='status'||event.type==='step'))setSteps(current=>[...current,event.message!])}
        if(done)break
      }
      sessionStorage.removeItem('forgepilot.pendingPrompt');sessionStorage.removeItem('forgepilot.pendingMode');sessionStorage.setItem('forgepilot.lastCreatedProject',project.id)
      setSteps(current=>[...current,'Project ready. Opening workspace…'])
      onComplete()
    }catch(e){setError((e as Error).message);setBusy(false)}
  }
  return <main className="handoff-shell"><section className="handoff-card"><div className="handoff-kicker">Authenticated · ready to {mode}</div><h1>{mode==='plan'?'Create the plan':'Build the application'}</h1><p className="handoff-prompt">{prompt}</p><div className="handoff-mode"><span className="selected">{mode==='plan'?'Plan mode':'Build mode'}</span><small>{mode==='plan'?'Architecture and implementation steps first.':'Create the project and execute implementation.'}</small></div>{steps.length>0&&<div className="handoff-steps">{steps.map((step,index)=><span key={`${step}-${index}`}>{index===steps.length-1&&busy?'◌':'✓'} {step}</span>)}</div>}{error&&<div className="handoff-error">{error}</div>}<div className="handoff-actions"><button disabled={busy} onClick={onCancel}>Back to workspace</button><button className="public-primary" disabled={busy} onClick={()=>void run()}>{busy?'Working…':mode==='plan'?'Create plan':'Build app'}</button></div></section></main>
}

function projectName(text:string){return text.trim().split(/\s+/).slice(0,6).join(' ')||'Untitled app'}
