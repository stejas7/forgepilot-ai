import {useEffect,useState} from 'react'

export type LoginIdentity={email:string;name:string}
type Provider={id:string;name:string;loginUrl:string}

export default function LoginPage({onLogin}:{onLogin:(identity:LoginIdentity)=>void}){
 const [providers,setProviders]=useState<Provider[]>([]),[error,setError]=useState('')
 useEffect(()=>{fetch('/api/auth/providers').then(r=>r.ok?r.json():[]).then((items:Provider[])=>setProviders(items.filter(p=>p.id==='google'||p.id==='github'))).catch(()=>setProviders([]))},[])
 function provider(id:'google'|'github'){const p=providers.find(x=>x.id===id);if(p){window.location.assign(p.loginUrl);return}setError(`${id==='google'?'Google':'GitHub'} sign-in is not configured on this deployment.`)}
 function recovery(){setError('Forgot password? Recover access with the provider you use to sign in: Google Account Recovery or GitHub password reset.')}
 return <main className="login-shell"><section className="login-brand"><div className="login-logo">F</div><div><span className="login-eyebrow">FORGEPILOT AI</span><h1>Build production apps with your AI engineering team.</h1><p>Prompt, build, review, secure and publish from one governed workspace.</p></div><small>Internal AI application platform</small></section><section className="login-stage"><div className="login-card"><div className="login-card-logo">F</div><h2>Welcome to ForgePilot</h2><p>Sign in securely with Google or GitHub.</p><div className="login-providers"><button onClick={()=>provider('google')}><b>G</b> Continue with Google</button><button onClick={()=>provider('github')}><b>⌘</b> Continue with GitHub</button></div><button className="login-recovery" type="button" onClick={recovery}>Forgot password?</button>{error&&<div className="login-error">{error}</div>}<p className="login-foot">ForgePilot does not store local passwords. Account recovery is handled by your sign-in provider.</p></div></section></main>
}
