import { useEffect, useState } from 'react'

type Plan={projectId:string;content:string;status:'DRAFT'|'APPROVED';updatedAt:string}

export default function PlanPanel({projectId,onApproved}:{projectId:string;onApproved:()=>void}){
  const [plan,setPlan]=useState<Plan|null>(null)
  const [draft,setDraft]=useState('')
  const [saving,setSaving]=useState(false)

  useEffect(()=>{void load()},[projectId])

  async function load(){
    const response=await fetch(`/api/projects/${projectId}/plan`)
    if(!response.ok){setPlan(null);setDraft('');return}
    const data:Plan=await response.json();setPlan(data);setDraft(data.content)
  }

  async function save(){
    setSaving(true)
    try{
      const response=await fetch(`/api/projects/${projectId}/plan`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({content:draft})})
      if(response.ok)setPlan(await response.json())
    }finally{setSaving(false)}
  }

  async function approve(){
    if(draft!==plan?.content)await save()
    const response=await fetch(`/api/projects/${projectId}/plan/approve`,{method:'POST'})
    if(response.ok){setPlan(await response.json());onApproved()}
  }

  if(!plan)return <div className="plan-panel empty">Run Plan to create an editable implementation plan.</div>
  return <div className="plan-panel"><div className="plan-head"><b>Implementation plan</b><span className={plan.status==='APPROVED'?'approved':''}>{plan.status}</span></div><textarea value={draft} onChange={e=>setDraft(e.target.value)} /><div className="plan-actions"><button disabled={saving||draft===plan.content} onClick={()=>void save()}>{saving?'Saving…':'Save plan'}</button><button className="approve" disabled={plan.status==='APPROVED'&&draft===plan.content} onClick={()=>void approve()}>{plan.status==='APPROVED'?'Approved':'Approve plan'}</button></div></div>
}
