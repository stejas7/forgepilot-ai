package io.forgepilot.publish;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.enterprise.EnterpriseGovernanceService;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.security.SecurityScanService;
import io.forgepilot.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Durable publish lifecycle: immutable releases, approvals, rollback and unpublish. */
@Service
public class PublishService {
    private static final String STATE = "publishes.json";
    private final PlatformStateStore store;
    private final WorkspaceService workspace;
    private final EnterpriseGovernanceService governance;
    private final SecurityScanService security;
    private final Map<UUID,List<Release>> releases;

    public PublishService(PlatformStateStore store, WorkspaceService workspace, EnterpriseGovernanceService governance, SecurityScanService security) {
        this.store=store; this.workspace=workspace; this.governance=governance; this.security=security;
        this.releases=new LinkedHashMap<>(store.read(STATE,new TypeReference<Map<UUID,List<Release>>>(){},LinkedHashMap::new));
    }

    public synchronized List<Release> history(UUID projectId){return List.copyOf(releases.getOrDefault(projectId,List.of()));}

    public synchronized Release request(UUID projectId,String actor,String visibility){
        String html=workspace.getFile(projectId,"preview/index.html").content();
        boolean approval=governance.settings().policy().publishApprovalRequired();
        Release release=new Release(UUID.randomUUID(),projectId,Instant.now(),actor,visibility==null?"WORKSPACE":visibility,
                approval?"PENDING_APPROVAL":"READY",false,html,"/published/"+projectId,null,null);
        releases.computeIfAbsent(projectId,k->new ArrayList<>()).add(0,release);persist();
        governance.append("PUBLISH_REQUESTED",projectId.toString(),Map.of("release",release.id().toString(),"approvalRequired",approval));
        return release;
    }

    public synchronized Release approve(UUID projectId,UUID releaseId,String approver){return transition(projectId,releaseId,"READY",approver,"PUBLISH_APPROVED");}
    public synchronized Release reject(UUID projectId,UUID releaseId,String approver){return transition(projectId,releaseId,"REJECTED",approver,"PUBLISH_REJECTED");}

    public synchronized Release publish(UUID projectId,UUID releaseId,String actor){
        Release current=find(projectId,releaseId);
        if(!"READY".equals(current.status()) && !"PUBLISHED".equals(current.status())) throw new IllegalStateException("Release requires approval before publish");
        SecurityScanService.GateDecision gate=security.gate(projectId);
        if(!gate.allowed()) throw new IllegalStateException(gate.message());
        List<Release> list=releases.get(projectId);
        for(int i=0;i<list.size();i++){Release r=list.get(i); if(r.published()) list.set(i,copy(r,r.status(),false,r.approvedBy(),r.publishedAt()));}
        Release updated=copy(current,"PUBLISHED",true,current.approvedBy(),Instant.now());replace(projectId,updated);persist();
        governance.append("PUBLISHED",projectId.toString(),Map.of("release",releaseId.toString(),"actor",actor,"securityScanId",String.valueOf(gate.scanId())));return updated;
    }

    public synchronized Release rollback(UUID projectId,UUID releaseId,String actor){
        Release target=find(projectId,releaseId);
        if("PENDING_APPROVAL".equals(target.status())||"REJECTED".equals(target.status())) throw new IllegalStateException("Cannot rollback to an unapproved release");
        return publish(projectId,releaseId,actor);
    }

    public synchronized void unpublish(UUID projectId,String actor){
        List<Release> list=releases.getOrDefault(projectId,new ArrayList<>());
        for(int i=0;i<list.size();i++){Release r=list.get(i);if(r.published())list.set(i,copy(r,"UNPUBLISHED",false,r.approvedBy(),r.publishedAt()));}
        persist();governance.append("UNPUBLISHED",projectId.toString(),Map.of("actor",actor));
    }

    public synchronized String publishedHtml(UUID projectId){return releases.getOrDefault(projectId,List.of()).stream().filter(Release::published).findFirst().orElseThrow(()->new IllegalStateException("Project is not published")).html();}

    private Release transition(UUID projectId,UUID id,String status,String actor,String audit){Release r=find(projectId,id);Release u=copy(r,status,false,actor,r.publishedAt());replace(projectId,u);persist();governance.append(audit,projectId.toString(),Map.of("release",id.toString(),"actor",actor));return u;}
    private Release find(UUID p,UUID id){return releases.getOrDefault(p,List.of()).stream().filter(r->r.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Release not found"));}
    private void replace(UUID p,Release u){List<Release> list=releases.get(p);for(int i=0;i<list.size();i++)if(list.get(i).id().equals(u.id())){list.set(i,u);return;}}
    private Release copy(Release r,String s,boolean pub,String approved,Instant at){return new Release(r.id(),r.projectId(),r.createdAt(),r.createdBy(),r.visibility(),s,pub,r.html(),r.publicPath(),approved,at);}
    private void persist(){store.write(STATE,releases);}

    public record Release(UUID id,UUID projectId,Instant createdAt,String createdBy,String visibility,String status,boolean published,String html,String publicPath,String approvedBy,Instant publishedAt){}
}
