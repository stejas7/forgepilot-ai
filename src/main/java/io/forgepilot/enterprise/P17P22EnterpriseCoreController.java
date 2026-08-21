package io.forgepilot.enterprise;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise/core")
public class P17P22EnterpriseCoreController {
    private final P17P22EnterpriseCoreService service;
    public P17P22EnterpriseCoreController(P17P22EnterpriseCoreService service){this.service=service;}

    // P17 Governance
    @GetMapping("/governance") public Object governance(){return service.governance();}
    @PutMapping("/governance/policies/{key}") public Object policy(@PathVariable String key,@RequestBody ValueRequest r){return service.setPolicy(key,r.value(),r.actor());}
    @PostMapping("/governance/workspaces") public Object workspace(@RequestBody WorkspaceRequest r){return service.addWorkspace(r.name(),r.parent(),r.actor());}
    @PostMapping("/governance/ownership/{projectId}") public Object owner(@PathVariable UUID projectId,@RequestBody OwnerRequest r){return service.assignOwner(projectId,r.owner(),r.actor());}
    @PostMapping("/governance/simulate") public Object simulate(@RequestBody SimulationRequest r){return service.simulate(r.action(),r.attributes());}

    // P18 Insights
    @GetMapping("/insights") public Object insights(){return service.insights();}
    @PutMapping("/insights/projects/{projectId}") public Object projectMeta(@PathVariable UUID projectId,@RequestBody ProjectMetaRequest r){return service.updateProjectMeta(projectId,r.owner(),r.pii(),r.externallyPublished(),r.requiresReview());}
    @GetMapping("/insights/export") public Object export(){return service.inventory();}

    // P19 Security Center
    @GetMapping("/security") public Object security(){return service.security();}
    @PutMapping("/security/posture/{control}") public Object posture(@PathVariable String control,@RequestBody ValueRequest r){return service.setSecurityPosture(control,r.value(),r.actor());}
    @PostMapping("/security/schedules") public Object schedule(@RequestBody ScanScheduleRequest r){return service.addScanSchedule(r.name(),r.cron(),r.depth(),r.actor());}
    @PostMapping("/security/findings") public Object finding(@RequestBody FindingRequest r){return service.addFinding(r.projectId(),r.category(),r.severity(),r.message(),r.owner(),r.slaHours());}
    @PutMapping("/security/findings/{id}") public Object findingStatus(@PathVariable UUID id,@RequestBody FindingStatusRequest r){return service.updateFinding(id,r.status(),r.evidence(),r.actor());}

    // P20 Audit/Trust
    @GetMapping("/audit") public Object audit(@RequestParam(defaultValue="") String q){return service.searchAudit(q);}
    @PostMapping("/audit/events") public Object event(@RequestBody AuditRequest r){return service.audit(r.actor(),r.action(),r.target(),r.detail());}
    @PutMapping("/audit/retention") public Object retention(@RequestBody RetentionRequest r){return service.setRetention(r.days(),r.actor());}
    @GetMapping("/trust") public Object trust(){return service.trust();}
    @PostMapping("/trust/evidence") public Object evidence(@RequestBody EvidenceRequest r){return service.addEvidence(r.control(),r.title(),r.reference(),r.owner());}
    @PostMapping("/trust/resources") public Object resource(@RequestBody ResourceRequest r){return service.addResource(r.type(),r.title(),r.reference());}

    // P21 Connector governance
    @GetMapping("/connectors") public Object connectors(){return service.connectors();}
    @PostMapping("/connectors/catalog") public Object connector(@RequestBody ConnectorRequest r){return service.upsertConnector(r.id(),r.name(),r.authType(),r.scope(),r.approvalRequired());}
    @PostMapping("/connectors/approvals") public Object connectorApproval(@RequestBody ConnectorApprovalRequest r){return service.requestConnector(r.connectorId(),r.projectId(),r.requester());}
    @PutMapping("/connectors/approvals/{id}") public Object connectorDecision(@PathVariable UUID id,@RequestBody ConnectorDecisionRequest r){return service.decideConnector(id,r.approved(),r.actor());}
    @PostMapping("/connectors/{id}/rotate") public Object rotate(@PathVariable String id,@RequestBody ActorRequest r){return service.rotateConnector(id,r.actor());}

    // P22 Design systems / brand governance
    @GetMapping("/design") public Object design(){return service.design();}
    @PostMapping("/design/systems") public Object designSystem(@RequestBody DesignSystemRequest r){return service.addDesignSystem(r.name(),r.tokens(),r.lockedPrimitives(),r.actor());}
    @PostMapping("/design/templates") public Object template(@RequestBody TemplateRequest r){return service.addTemplate(r.name(),r.description(),r.designSystemId(),r.authShell(),r.actor());}
    @PostMapping("/design/check") public Object brandCheck(@RequestBody BrandCheckRequest r){return service.brandCheck(r.designSystemId(),r.tokens());}

    public record ValueRequest(String value,String actor){}
    public record ActorRequest(String actor){}
    public record WorkspaceRequest(String name,String parent,String actor){}
    public record OwnerRequest(String owner,String actor){}
    public record SimulationRequest(String action,Map<String,String> attributes){}
    public record ProjectMetaRequest(String owner,boolean pii,boolean externallyPublished,boolean requiresReview){}
    public record ScanScheduleRequest(String name,String cron,String depth,String actor){}
    public record FindingRequest(UUID projectId,String category,String severity,String message,String owner,int slaHours){}
    public record FindingStatusRequest(String status,String evidence,String actor){}
    public record AuditRequest(String actor,String action,String target,String detail){}
    public record RetentionRequest(int days,String actor){}
    public record EvidenceRequest(String control,String title,String reference,String owner){}
    public record ResourceRequest(String type,String title,String reference){}
    public record ConnectorRequest(String id,String name,String authType,String scope,boolean approvalRequired){}
    public record ConnectorApprovalRequest(String connectorId,UUID projectId,String requester){}
    public record ConnectorDecisionRequest(boolean approved,String actor){}
    public record DesignSystemRequest(String name,Map<String,String> tokens,List<String> lockedPrimitives,String actor){}
    public record TemplateRequest(String name,String description,UUID designSystemId,String authShell,String actor){}
    public record BrandCheckRequest(UUID designSystemId,Map<String,String> tokens){}
}

@Service
class P17P22EnterpriseCoreService {
    private static final String FILE="p17-p22-enterprise-core.json";
    private final PlatformStateStore store; private final ProjectService projects; private State state;
    P17P22EnterpriseCoreService(PlatformStateStore store,ProjectService projects){this.store=store;this.projects=projects;this.state=store.read(FILE,new TypeReference<State>(){},State::empty);}

    synchronized GovernanceView governance(){return new GovernanceView(List.copyOf(state.workspaces()),Map.copyOf(state.policies()),Map.copyOf(state.owners()),List.copyOf(state.policyAudit()));}
    synchronized PolicyChange setPolicy(String key,String value,String actor){state.policies().put(key,value);PolicyChange c=new PolicyChange(Instant.now(),actor,key,value);state.policyAudit().add(0,c);audit(actor,"POLICY_CHANGE",key,value);persist();return c;}
    synchronized WorkspaceNode addWorkspace(String name,String parent,String actor){WorkspaceNode w=new WorkspaceNode(UUID.randomUUID(),name,parent,Instant.now());state.workspaces().add(w);audit(actor,"WORKSPACE_CREATE",w.id().toString(),name);persist();return w;}
    synchronized Ownership assignOwner(UUID projectId,String owner,String actor){projects.findById(projectId);state.owners().put(projectId,owner);Ownership o=new Ownership(projectId,owner,Instant.now());audit(actor,"OWNERSHIP_ASSIGN",projectId.toString(),owner);persist();return o;}
    synchronized Simulation simulate(String action,Map<String,String> attributes){List<String> reasons=new ArrayList<>();String visibility=state.policies().getOrDefault("project.visibility","PRIVATE");if("PUBLISH_PUBLIC".equalsIgnoreCase(action)&&"PRIVATE".equalsIgnoreCase(visibility))reasons.add("Workspace policy keeps projects private by default");String allow=state.policies().get("publishing.allowed");if("false".equalsIgnoreCase(allow)&&action.toUpperCase(Locale.ROOT).contains("PUBLISH"))reasons.add("Publishing disabled by policy");return new Simulation(action,reasons.isEmpty(),List.copyOf(reasons),attributes==null?Map.of():Map.copyOf(attributes));}

    synchronized Insights insights(){List<Project> all=projects.findAll();long active=all.stream().filter(p->p.status()!=Project.ProjectStatus.DRAFT).count();long ext=state.projectMeta().values().stream().filter(ProjectMeta::externallyPublished).count();long review=state.projectMeta().values().stream().filter(ProjectMeta::requiresReview).count();long pii=state.projectMeta().values().stream().filter(ProjectMeta::pii).count();long missing=all.stream().filter(p->!state.owners().containsKey(p.id())&&(state.projectMeta().get(p.id())==null||blank(state.projectMeta().get(p.id()).owner()))).count();long open=state.findings().stream().filter(f->!"RESOLVED".equals(f.status())).count();return new Insights(all.size(),active,ext,review,pii,missing,open,Instant.now());}
    synchronized ProjectMeta updateProjectMeta(UUID id,String owner,boolean pii,boolean ext,boolean review){projects.findById(id);ProjectMeta m=new ProjectMeta(id,owner,pii,ext,review,Instant.now());state.projectMeta().put(id,m);if(!blank(owner))state.owners().put(id,owner);persist();return m;}
    synchronized List<ProjectInventory> inventory(){return projects.findAll().stream().map(p->{ProjectMeta m=state.projectMeta().get(p.id());return new ProjectInventory(p.id(),p.name(),p.status().name(),state.owners().getOrDefault(p.id(),m==null?null:m.owner()),m!=null&&m.pii(),m!=null&&m.externallyPublished(),m!=null&&m.requiresReview());}).toList();}

    synchronized SecurityView security(){long open=state.findings().stream().filter(f->!"RESOLVED".equals(f.status())).count();long critical=state.findings().stream().filter(f->!"RESOLVED".equals(f.status())&&"CRITICAL".equalsIgnoreCase(f.severity())).count();return new SecurityView(Map.copyOf(state.securityPosture()),List.copyOf(state.scanSchedules()),List.copyOf(state.findings()),open,critical);}
    synchronized Map<String,String> setSecurityPosture(String control,String value,String actor){state.securityPosture().put(control,value);audit(actor,"SECURITY_POSTURE",control,value);persist();return Map.copyOf(state.securityPosture());}
    synchronized ScanSchedule addScanSchedule(String name,String cron,String depth,String actor){ScanSchedule s=new ScanSchedule(UUID.randomUUID(),name,cron,depth==null?"DEEP":depth.toUpperCase(Locale.ROOT),true,Instant.now());state.scanSchedules().add(s);audit(actor,"SCAN_SCHEDULE_CREATE",s.id().toString(),cron);persist();return s;}
    synchronized EnterpriseFinding addFinding(UUID projectId,String category,String severity,String message,String owner,int slaHours){EnterpriseFinding f=new EnterpriseFinding(UUID.randomUUID(),projectId,category,severity==null?"MEDIUM":severity.toUpperCase(Locale.ROOT),message,owner,"OPEN",Math.max(1,slaHours),null,Instant.now(),Instant.now());state.findings().add(0,f);persist();return f;}
    synchronized EnterpriseFinding updateFinding(UUID id,String status,String evidence,String actor){for(int i=0;i<state.findings().size();i++){EnterpriseFinding f=state.findings().get(i);if(f.id().equals(id)){EnterpriseFinding u=new EnterpriseFinding(f.id(),f.projectId(),f.category(),f.severity(),f.message(),f.owner(),status==null?f.status():status.toUpperCase(Locale.ROOT),f.slaHours(),evidence,f.createdAt(),Instant.now());state.findings().set(i,u);audit(actor,"FINDING_UPDATE",id.toString(),u.status());persist();return u;}}throw new IllegalArgumentException("Finding not found");}

    synchronized AuditEvent audit(String actor,String action,String target,String detail){AuditEvent e=new AuditEvent(UUID.randomUUID(),Instant.now(),actor,action,target,detail);state.audit().add(0,e);trimAudit();persist();return e;}
    synchronized List<AuditEvent> searchAudit(String q){String n=q==null?"":q.toLowerCase(Locale.ROOT);return state.audit().stream().filter(e->n.isBlank()||(String.valueOf(e.actor())+" "+e.action()+" "+e.target()+" "+e.detail()).toLowerCase(Locale.ROOT).contains(n)).toList();}
    synchronized Retention setRetention(int days,String actor){state.retentionDays(Math.max(30,Math.min(days,3650)));audit(actor,"RETENTION_CHANGE","audit",String.valueOf(state.retentionDays()));persist();return new Retention(state.retentionDays(),Instant.now());}
    synchronized TrustView trust(){return new TrustView(List.copyOf(state.evidence()),List.copyOf(state.resources()),state.retentionDays(),"NO_CERTIFICATION_CLAIMS",Instant.now());}
    synchronized Evidence addEvidence(String control,String title,String reference,String owner){Evidence e=new Evidence(UUID.randomUUID(),control,title,reference,owner,Instant.now());state.evidence().add(e);persist();return e;}
    synchronized TrustResource addResource(String type,String title,String reference){TrustResource r=new TrustResource(UUID.randomUUID(),type,title,reference,Instant.now());state.resources().add(r);persist();return r;}

    synchronized ConnectorView connectors(){return new ConnectorView(Map.copyOf(state.connectorCatalog()),List.copyOf(state.connectorApprovals()),Map.copyOf(state.connectorRotations()));}
    synchronized ConnectorDefinition upsertConnector(String id,String name,String authType,String scope,boolean approvalRequired){ConnectorDefinition c=new ConnectorDefinition(id,name,authType,scope,approvalRequired,Instant.now());state.connectorCatalog().put(id,c);persist();return c;}
    synchronized ConnectorApproval requestConnector(String id,UUID projectId,String requester){if(!state.connectorCatalog().containsKey(id))throw new IllegalArgumentException("Connector not in workspace catalog");ConnectorApproval a=new ConnectorApproval(UUID.randomUUID(),id,projectId,requester,"PENDING",null,Instant.now(),Instant.now());state.connectorApprovals().add(0,a);persist();return a;}
    synchronized ConnectorApproval decideConnector(UUID id,boolean approved,String actor){for(int i=0;i<state.connectorApprovals().size();i++){ConnectorApproval a=state.connectorApprovals().get(i);if(a.id().equals(id)){ConnectorApproval u=new ConnectorApproval(a.id(),a.connectorId(),a.projectId(),a.requester(),approved?"APPROVED":"REJECTED",actor,a.createdAt(),Instant.now());state.connectorApprovals().set(i,u);audit(actor,"CONNECTOR_DECISION",id.toString(),u.status());persist();return u;}}throw new IllegalArgumentException("Approval not found");}
    synchronized Rotation rotateConnector(String id,String actor){if(!state.connectorCatalog().containsKey(id))throw new IllegalArgumentException("Connector not found");Rotation r=new Rotation(id,Instant.now(),Instant.now().plusSeconds(90L*24*3600),actor);state.connectorRotations().put(id,r);audit(actor,"CONNECTOR_ROTATE",id,"rotated");persist();return r;}

    synchronized DesignView design(){return new DesignView(List.copyOf(state.designSystems()),List.copyOf(state.templates()));}
    synchronized DesignSystem addDesignSystem(String name,Map<String,String> tokens,List<String> locked,String actor){DesignSystem d=new DesignSystem(UUID.randomUUID(),name,tokens==null?Map.of():Map.copyOf(tokens),locked==null?List.of():List.copyOf(locked),Instant.now());state.designSystems().add(d);audit(actor,"DESIGN_SYSTEM_CREATE",d.id().toString(),name);persist();return d;}
    synchronized AppTemplate addTemplate(String name,String description,UUID designSystemId,String authShell,String actor){designSystem(designSystemId);AppTemplate t=new AppTemplate(UUID.randomUUID(),name,description,designSystemId,authShell,Instant.now());state.templates().add(t);audit(actor,"TEMPLATE_CREATE",t.id().toString(),name);persist();return t;}
    synchronized BrandCheck brandCheck(UUID designSystemId,Map<String,String> tokens){DesignSystem d=designSystem(designSystemId);Map<String,String> actual=tokens==null?Map.of():tokens;List<String> violations=new ArrayList<>();d.lockedPrimitives().forEach(k->{String expected=d.tokens().get(k),got=actual.get(k);if(expected!=null&&!Objects.equals(expected,got))violations.add(k+" must equal "+expected);});return new BrandCheck(violations.isEmpty(),List.copyOf(violations));}

    private DesignSystem designSystem(UUID id){return state.designSystems().stream().filter(d->d.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Design system not found"));}
    private void trimAudit(){int max=Math.max(500,Math.min(state.retentionDays()*20,20000));while(state.audit().size()>max)state.audit().remove(state.audit().size()-1);}
    private boolean blank(String v){return v==null||v.isBlank();}
    private void persist(){store.write(FILE,state);}

    record GovernanceView(List<WorkspaceNode> workspaces,Map<String,String> policies,Map<UUID,String> owners,List<PolicyChange> audit){}
    record WorkspaceNode(UUID id,String name,String parent,Instant createdAt){}
    record PolicyChange(Instant at,String actor,String key,String value){}
    record Ownership(UUID projectId,String owner,Instant updatedAt){}
    record Simulation(String action,boolean allowed,List<String> reasons,Map<String,String> attributes){}
    record ProjectMeta(UUID projectId,String owner,boolean pii,boolean externallyPublished,boolean requiresReview,Instant updatedAt){}
    record Insights(long totalApps,long activeApps,long externallyPublished,long requiringReview,long piiApps,long missingOwner,long openFindings,Instant generatedAt){}
    record ProjectInventory(UUID id,String name,String status,String owner,boolean pii,boolean externallyPublished,boolean requiresReview){}
    record SecurityView(Map<String,String> posture,List<ScanSchedule> schedules,List<EnterpriseFinding> findings,long open,long critical){}
    record ScanSchedule(UUID id,String name,String cron,String depth,boolean enabled,Instant createdAt){}
    record EnterpriseFinding(UUID id,UUID projectId,String category,String severity,String message,String owner,String status,int slaHours,String evidence,Instant createdAt,Instant updatedAt){}
    record AuditEvent(UUID id,Instant at,String actor,String action,String target,String detail){}
    record Retention(int days,Instant updatedAt){}
    record Evidence(UUID id,String control,String title,String reference,String owner,Instant createdAt){}
    record TrustResource(UUID id,String type,String title,String reference,Instant createdAt){}
    record TrustView(List<Evidence> evidence,List<TrustResource> resources,int retentionDays,String certificationPolicy,Instant generatedAt){}
    record ConnectorDefinition(String id,String name,String authType,String scope,boolean approvalRequired,Instant updatedAt){}
    record ConnectorApproval(UUID id,String connectorId,UUID projectId,String requester,String status,String decidedBy,Instant createdAt,Instant updatedAt){}
    record Rotation(String connectorId,Instant rotatedAt,Instant expiresAt,String actor){}
    record ConnectorView(Map<String,ConnectorDefinition> catalog,List<ConnectorApproval> approvals,Map<String,Rotation> rotations){}
    record DesignSystem(UUID id,String name,Map<String,String> tokens,List<String> lockedPrimitives,Instant createdAt){}
    record AppTemplate(UUID id,String name,String description,UUID designSystemId,String authShell,Instant createdAt){}
    record DesignView(List<DesignSystem> systems,List<AppTemplate> templates){}
    record BrandCheck(boolean passed,List<String> violations){}

    static final class State {
        private final List<WorkspaceNode> workspaces; private final Map<String,String> policies; private final Map<UUID,String> owners; private final List<PolicyChange> policyAudit; private final Map<UUID,ProjectMeta> projectMeta; private final Map<String,String> securityPosture; private final List<ScanSchedule> scanSchedules; private final List<EnterpriseFinding> findings; private final List<AuditEvent> audit; private int retentionDays; private final List<Evidence> evidence; private final List<TrustResource> resources; private final Map<String,ConnectorDefinition> connectorCatalog; private final List<ConnectorApproval> connectorApprovals; private final Map<String,Rotation> connectorRotations; private final List<DesignSystem> designSystems; private final List<AppTemplate> templates;
        State(List<WorkspaceNode> workspaces,Map<String,String> policies,Map<UUID,String> owners,List<PolicyChange> policyAudit,Map<UUID,ProjectMeta> projectMeta,Map<String,String> securityPosture,List<ScanSchedule> scanSchedules,List<EnterpriseFinding> findings,List<AuditEvent> audit,int retentionDays,List<Evidence> evidence,List<TrustResource> resources,Map<String,ConnectorDefinition> connectorCatalog,List<ConnectorApproval> connectorApprovals,Map<String,Rotation> connectorRotations,List<DesignSystem> designSystems,List<AppTemplate> templates){this.workspaces=workspaces;this.policies=policies;this.owners=owners;this.policyAudit=policyAudit;this.projectMeta=projectMeta;this.securityPosture=securityPosture;this.scanSchedules=scanSchedules;this.findings=findings;this.audit=audit;this.retentionDays=retentionDays;this.evidence=evidence;this.resources=resources;this.connectorCatalog=connectorCatalog;this.connectorApprovals=connectorApprovals;this.connectorRotations=connectorRotations;this.designSystems=designSystems;this.templates=templates;}
        static State empty(){Map<String,String> p=new LinkedHashMap<>();p.put("project.visibility","PRIVATE");p.put("publishing.allowed","true");return new State(new ArrayList<>(),p,new LinkedHashMap<>(),new ArrayList<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),365,new ArrayList<>(),new ArrayList<>(),new LinkedHashMap<>(),new ArrayList<>(),new LinkedHashMap<>(),new ArrayList<>(),new ArrayList<>());}
        List<WorkspaceNode> workspaces(){return workspaces;} Map<String,String> policies(){return policies;} Map<UUID,String> owners(){return owners;} List<PolicyChange> policyAudit(){return policyAudit;} Map<UUID,ProjectMeta> projectMeta(){return projectMeta;} Map<String,String> securityPosture(){return securityPosture;} List<ScanSchedule> scanSchedules(){return scanSchedules;} List<EnterpriseFinding> findings(){return findings;} List<AuditEvent> audit(){return audit;} int retentionDays(){return retentionDays;} void retentionDays(int v){retentionDays=v;} List<Evidence> evidence(){return evidence;} List<TrustResource> resources(){return resources;} Map<String,ConnectorDefinition> connectorCatalog(){return connectorCatalog;} List<ConnectorApproval> connectorApprovals(){return connectorApprovals;} Map<String,Rotation> connectorRotations(){return connectorRotations;} List<DesignSystem> designSystems(){return designSystems;} List<AppTemplate> templates(){return templates;}
    }
}