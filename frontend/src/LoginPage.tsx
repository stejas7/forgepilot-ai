import {useEffect,useState} from 'react'

export type LoginIdentity={email:string;name:string}
type Provider={id:string;name:string;loginUrl:string}

const GOOGLE_RECOVERY='https://support.google.com/accounts/answer/7682439'
const GITHUB_RECOVERY='https://github.com/password_reset'

export default function LoginPage({onLogin}:{onLogin:(identity:LoginIdentity)=>void}){
 const [providers,setProviders]=useState<Provider[]>([]),[error,setError]=useState(''),[recoveryOpen,setRecoveryOpen]=useState(false)
 useEffect(()=>{fetch('/api/auth/providers').then(r=>r.ok?r.json():[]).then((items:Provider[])=>setProviders(items.filter(p=>p.id==='google'||p.id==='github'))).catch(()=>setProviders([]))},[])
 function provider(id:'google'|'github'){const p=providers.find(x=>x.id===id);if(p){window.location.assign(p.loginUrl);return}setError(`${id==='google'?'Google':'GitHub'} sign-in is not configured on this deployment.`)}
 return <main className="login-shell"><section className="login-brand"><div className="login-logo">F</div><div><span className="login-eyebrow">FORGEPILOT AI</span><h1>Build production apps with your AI engineering team.</h1><p>Prompt, build, review, secure and publish from one governed workspace.</p></div><small>Internal AI application platform</small></section><section className="login-stage"><div className="login-card"><div className="login-card-logo">F</div><h2>Welcome to ForgePilot</h2><p>Sign in securely with Google or GitHub.</p><div className="login-providers"><button onClick={()=>provider('google')}><b>G</b> Continue with Google</button><button onClick={()=>provider('github')}><b>⌘</b> Continue with GitHub</button></div><button className="login-recovery" type="button" onClick={()=>setRecoveryOpen(v=>!v)}>Forgot password?</button>{recoveryOpen&&<div className="login-recovery-panel"><p>ForgePilot does not store passwords. Recover your account with the provider you use:</p><a href={GOOGLE_RECOVERY} target="_blank" rel="noreferrer">Recover Google account</a><a href={GITHUB_RECOVERY} target="_blank" rel="noreferrer">Reset GitHub password</a></div>}{error&&<div className="login-error">{error}</div>}<p className="login-foot">Authentication and password recovery are handled by Google or GitHub.</p></div></section></main>
}
