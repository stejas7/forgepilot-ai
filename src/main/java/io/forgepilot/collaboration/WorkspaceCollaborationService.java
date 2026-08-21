package io.forgepilot.collaboration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.enterprise.EnterpriseGovernanceService;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/** Durable workspace membership, RBAC, project sharing, comments and activity. */
@Service
public class WorkspaceCollaborationService {
    private static final String STATE="collaboration.json";
    private final PlatformStateStore store;
    private final EnterpriseGovernanceService governance;
    private State state;

    public WorkspaceCollaborationService(PlatformStateStore store,EnterpriseGovernanceService governance){
        this.store=store;this.governance=governance;
        this.state=store.read(STATE,new TypeReference<State>(){},()->new State(new ArrayList<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new ArrayList<>()));
    }

    public synchronized State state(){return state;}
    public synchronized Member invite(String email,Role role,String actor){Member m=new Member(UUID.randomUUID(),email,role,"INVITED",Instant.now());state.members().add(m);activity(actor,"MEMBER_INVITED",email);persist();return m;}
    public synchronized Member setRole(UUID id,Role role,String actor){for(int i=0;i<state.members().size();i++){Member m=state.members().get(i);if(m.id().equals(id)){Member u=new Member(m.id(),m.email(),role,m.status(),m.createdAt());state.members().set(i,u);activity(actor,"ROLE_CHANGED",m.email()+":"+role);persist();return u;}}throw new IllegalArgumentException("Member not found");}
    public synchronized void share(UUID projectId,String email,Role role,String actor){state.projectAccess().computeIfAbsent(projectId,k->new LinkedHashMap<>()).put(email,role);activity(actor,"PROJECT_SHARED",projectId+":"+email);persist();}
    public synchronized Comment comment(UUID projectId,String actor,String body,String target){Comment c=new Comment(UUID.randomUUID(),projectId,actor,body,target,Instant.now());state.comments().computeIfAbsent(projectId,k->new ArrayList<>()).add(c);activity(actor,"COMMENTED",projectId.toString());persist();return c;}
    public synchronized List<Activity> activity(){return List.copyOf(state.activity());}
    public boolean allowed(Role role,String action){return switch(role){case OWNER,ADMIN->true;case EDITOR->Set.of("EDIT","COMMENT","BUILD").contains(action);case APPROVER->Set.of("APPROVE","COMMENT","VIEW").contains(action);case PUBLISHER->Set.of("PUBLISH","COMMENT","VIEW").contains(action);case VIEWER->Set.of("VIEW","COMMENT").contains(action);};}
    private void activity(String actor,String action,String target){Activity a=new Activity(UUID.randomUUID(),Instant.now(),actor,action,target);state.activity().add(0,a);governance.append(action,target,Map.of("actor",actor));}
    private void persist(){store.write(STATE,state);}

    public enum Role{OWNER,ADMIN,EDITOR,VIEWER,APPROVER,PUBLISHER}
    public record Member(UUID id,String email,Role role,String status,Instant createdAt){}
    public record Comment(UUID id,UUID projectId,String actor,String body,String target,Instant createdAt){}
    public record Activity(UUID id,Instant at,String actor,String action,String target){}
    public record State(List<Member> members,Map<UUID,Map<String,Role>> projectAccess,Map<UUID,List<Comment>> comments,List<Activity> activity){}
}
