package io.forgepilot.knowledge;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/** P12 durable knowledge, reusable skills/templates and queued agent work. */
@Service
public class KnowledgeAgentService {
    private static final String STATE="knowledge-agent.json";
    private final PlatformStateStore store;
    private State state;

    public KnowledgeAgentService(PlatformStateStore store){
        this.store=store;
        this.state=store.read(STATE,new TypeReference<State>(){},KnowledgeAgentService::defaults);
    }

    public synchronized State state(){return state;}
    public synchronized KnowledgeItem addKnowledge(String scope,String title,String content,List<String> tags){
        KnowledgeItem item=new KnowledgeItem(UUID.randomUUID(),scope==null?"WORKSPACE":scope,title,content,tags==null?List.of():List.copyOf(tags),Instant.now());
        state.knowledge().add(0,item);persist();return item;
    }
    public synchronized List<KnowledgeHit> retrieve(String query,int limit){
        String[] terms=(query==null?"":query.toLowerCase()).split("\\s+");
        return state.knowledge().stream().map(k->new KnowledgeHit(k,score(k,terms))).filter(h->h.score()>0)
                .sorted(Comparator.comparingInt(KnowledgeHit::score).reversed()).limit(Math.max(1,limit)).toList();
    }
    public synchronized Template addTemplate(String name,String description,String prompt,String stack){Template t=new Template(UUID.randomUUID(),name,description,prompt,stack,Instant.now());state.templates().add(0,t);persist();return t;}
    public synchronized Skill addSkill(String name,String instruction,boolean enabled){Skill s=new Skill(UUID.randomUUID(),name,instruction,enabled,Instant.now());state.skills().add(0,s);persist();return s;}
    public synchronized AgentTask enqueue(UUID projectId,String prompt,String agent){AgentTask t=new AgentTask(UUID.randomUUID(),projectId,prompt,agent==null?"GENERAL":agent,"QUEUED",Instant.now(),null,null);state.queue().add(t);persist();return t;}
    public synchronized AgentTask transition(UUID id,String status,String result){for(int i=0;i<state.queue().size();i++){AgentTask t=state.queue().get(i);if(t.id().equals(id)){AgentTask u=new AgentTask(t.id(),t.projectId(),t.prompt(),t.agent(),status,t.createdAt(),Instant.now(),result);state.queue().set(i,u);persist();return u;}}throw new IllegalArgumentException("Agent task not found");}
    public synchronized ContextBundle context(String query){return new ContextBundle(retrieve(query,6),state.skills().stream().filter(Skill::enabled).toList(),Instant.now());}
    private int score(KnowledgeItem k,String[] terms){String text=(k.title()+" "+k.content()+" "+String.join(" ",k.tags())).toLowerCase();int s=0;for(String t:terms)if(!t.isBlank()&&text.contains(t))s++;return s;}
    private void persist(){store.write(STATE,state);}
    private static State defaults(){return new State(new ArrayList<>(),new ArrayList<>(List.of(new Template(UUID.randomUUID(),"Internal tool","Operations app starter","Build a secure internal operations tool with forms, approvals and audit history.","React + Spring Boot + PostgreSQL",Instant.now()))),new ArrayList<>(),new ArrayList<>());}

    public record KnowledgeItem(UUID id,String scope,String title,String content,List<String> tags,Instant createdAt){}
    public record KnowledgeHit(KnowledgeItem item,int score){}
    public record Template(UUID id,String name,String description,String prompt,String stack,Instant createdAt){}
    public record Skill(UUID id,String name,String instruction,boolean enabled,Instant createdAt){}
    public record AgentTask(UUID id,UUID projectId,String prompt,String agent,String status,Instant createdAt,Instant updatedAt,String result){}
    public record ContextBundle(List<KnowledgeHit> knowledge,List<Skill> skills,Instant generatedAt){}
    public record State(List<KnowledgeItem> knowledge,List<Template> templates,List<Skill> skills,List<AgentTask> queue){}
}
