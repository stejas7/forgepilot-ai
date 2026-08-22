import React,{useEffect,useState} from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import LoginPage from './LoginPage'
import './styles.css'
import './login.css'
import './editor.css'
import './p2.css'
import './p5.css'
import './p6.css'
import './p7.css'
import './p11.css'

type Session={authenticated:boolean;name:string;authorities:string[]}
function Root(){
 const [session,setSession]=useState<Session|null>(null),[oauthEnabled,setOauthEnabled]=useState(false)
 useEffect(()=>{Promise.all([fetch('/api/auth/providers').then(r=>r.ok?r.json():[]),fetch('/api/auth/me').then(r=>r.ok?r.json():{authenticated:false,name:'',authorities:[]})]).then(([providers,me])=>{setOauthEnabled(providers.length>0);setSession(me)}).catch(()=>setSession({authenticated:false,name:'',authorities:[]}))},[])
 if(!session)return <div className="login-shell"><section className="login-stage"><div className="login-card"><h2>Loading ForgePilot…</h2></div></section></div>
 if(oauthEnabled&&!session.authenticated)return <LoginPage onLogin={()=>window.location.reload()}/>
 return <App/>
}
ReactDOM.createRoot(document.getElementById('root')!).render(<React.StrictMode><Root/></React.StrictMode>)
