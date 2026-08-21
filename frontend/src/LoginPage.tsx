import {useEffect,useState} from 'react'

export type LoginIdentity={email:string;name:string}
type Provider={id:string;name:string;loginUrl:string}

export default function LoginPage({onLogin}:{onLogin:(identity:LoginIdentity)=>void}){
 const [email,setEmail]=useState(''),[providers,setProviders]=useState<Provider[]>([]),[error,setError]=useState('')
 useEffect(()=>{fetch('/api/auth/providers').then(r=>r.ok?r.json():[]).then(setProviders).catch(()=>setProviders([]))},[])
 function provider(id:string){const p=providers.find(x=>x.id===id);if(p){window.location.assign(p.loginUrl);return}setError(`${id} SSO is not configured on this deployment.`)}
 return <main className="login-shell"><section className="login-brand"><div className="login-logo">F</div><div><span className="login-eyebrow">FORGEPILOT AI</span><h1>Build production apps with your AI engineering team.</h1><p>Prompt, build, review, secure and publish from one governed workspace.</p></div><small>Internal AI application platform</small></section><section className="login-stage"><div className="login-card"><div className="login-card-logo">F</div><h2>Welcome to ForgePilot</h2><p>Sign in with your organization identity.</p><div className="login-providers"><button onClick={()=>provider('google')}><b>G</b> Continue with Google</button><button onClick={()=>provider('github')}><b>⌘</b> Continue with GitHub</button><button onClick={()=>provider('sso')}><b>↗</b> Continue with SSO</button></div><div className="login-divider"><span>enterprise access</span></div><label>Work email<input type="email" autoComplete="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@company.com"/></label>{error&&<div className="login-error">{error}</div>}<p className="login-foot">Available providers: {providers.length?providers.map(p=>p.name).join(', '):'none configured'}.</p></div></section></main>
}
