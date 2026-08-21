import { useRef,useState } from 'react'

type Attachment={id:string;name:string;mimeType:string;size:number}

export default function AttachmentPicker({projectId}:{projectId:string}){
  const inputRef=useRef<HTMLInputElement|null>(null)
  const [items,setItems]=useState<Attachment[]>([])
  const [uploading,setUploading]=useState(false)

  async function upload(file:File){
    const form=new FormData();form.append('file',file)
    setUploading(true)
    try{
      const response=await fetch(`/api/projects/${projectId}/attachments`,{method:'POST',body:form})
      if(!response.ok)throw new Error('Upload failed')
      const value=await response.json()
      setItems(current=>[...current,{id:value.id,name:value.name,mimeType:value.mimeType,size:value.size}])
    }finally{setUploading(false)}
  }

  return <div className="attachment-picker"><input ref={inputRef} type="file" hidden accept="image/*,.txt,.md,.json,.csv,.xml,.yaml,.yml" onChange={e=>{const file=e.target.files?.[0];if(file)void upload(file);e.currentTarget.value=''}}/><button type="button" title="Attach image or context file" disabled={uploading} onClick={()=>inputRef.current?.click()}>{uploading?'…':'＋'}</button>{items.slice(-2).map(item=><span key={item.id} title={item.name}>{item.name}</span>)}</div>
}
