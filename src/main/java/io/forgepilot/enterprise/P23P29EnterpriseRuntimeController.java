package io.forgepilot.enterprise;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise/runtime")
public class P23P29EnterpriseRuntimeController {
    private final P23P29EnterpriseRuntimeService service;
    public P23P29EnterpriseRuntimeController(P23P29EnterpriseRuntimeService service){this.service=service;}

    // P23 Agent runtime
    @GetMapping("/agents/jobs") public Object jobs(){return service.jobs();}
    @PostMapping("/agents/jobs") public Object enqueue(@RequestBody AgentJobRequest r){return service.enqueue(r.projectId(),r.prompt(),r.maxRetries(),r.timeoutSeconds(),r.approvalRequired());}
    @PostMapping("/agents/jobs/{id}/step") public Object step(@PathVariable UUID id,@RequestBody AgentStepRequest r){return service.agentStep(id,r.phase(),r.message(),r.costUnits());}
    @PostMapping("/agents/jobs/{id}/approve") public Object approve(@PathVariable UUID id,@RequestBody ActorRequest r){return service.approveJob(id,r.actor());}
    @PostMapping("/agents/jobs/{id}/cancel") public Object cancel(@PathVariable UUID id,@RequestBody ActorRequest r){return service.cancelJob(id,r.actor());}
    @PostMapping("/agents/mcp") public Object mcp(@RequestBody McpRequest r){return service.registerMcp(r.name(),r.endpoint(),r.scope(),r.enabled());}

    // P24 Data platform
    @GetMapping("/data") public Object data(){return service.data();}
    @PostMapping("/data/environments") public Object environment(@RequestBody DataEnvironmentRequest r){return service.environment(r.projectId(),r.name(),r.region(),r.quotaMb());}
    @PostMapping("/data/backups") public Object backup(@RequestBody BackupRequest r){return service.backup(r.projectId(),r.environment(),r.type());}
    @PostMapping("/data/backups/{id}/restore-test") public Object restoreTest(@PathVariable UUID id,@RequestBody ActorRequest r){return service.restoreTest(id,r.actor());}
    @PostMapping("/data/classifications") public Object classification(@RequestBody ClassificationRequest r){return service.classify(r.projectId(),r.resource(),r.classification(),r.pii());}
    @PostMapping("/data/migrations") public Object migration(@RequestBody MigrationRequest r){return service.migration(r.projectId(),r.environment(),r.version(),r.description(),r.safe());}

    // P25 Runtime/deployment
    @GetMapping("/deployments") public Object deployments(){return service.deployments();}
    @PutMapping("/deployments/limits/{projectId}") public Object limits(@PathVariable UUID projectId,@RequestBody RuntimeLimitRequest r){return service.limits(projectId,r.cpuMillis(),r.memoryMb(),r.timeoutSeconds(),r.networkMode());}
    @PostMapping("/deployments/artifacts") public Object artifact(@RequestBody ArtifactRequest r){return service.artifact(r.projectId(),r.environment(),r.digest(),r.sourceVersion());}
    @PostMapping("/deployments/releases") public Object release(@RequestBody DeploymentRequest r){return service.deploy(r.projectId(),r.environment(),r.artifactId(),r.strategy());}
    @PostMapping("/deployments/{id}/rollback") public Object rollback(@PathVariable UUID id,@RequestBody ActorRequest r){return service.rollback(id,r.actor());}
    @PostMapping("/deployments/domains") public Object domain(@RequestBody DomainRequest r){return service.domain(r.projectId(),r.domain(),r.verificationToken());}
    @PostMapping("/deployments/domains/{id}/verify") public Object verifyDomain(@PathVariable UUID id,@RequestBody DomainVerifyRequest r){return service.verifyDomain(id,r.observedToken());}

    // P26 Collaboration 2.0
    @GetMapping("/collaboration") public Object collaboration(){return service.collaboration();}
    @PutMapping("/collaboration/presence") public Object presence(@RequestBody PresenceRequest r){return service.presence(r.user(),r.projectId(),r.resource());}
    @PostMapping("/collaboration/threads") public Object thread(@RequestBody ThreadRequest r){return service.thread(r.projectId(),r.target(),r.author(),r.body(),r.mentions());}
    @PostMapping("/collaboration/access-requests") public Object access(@RequestBody AccessRequest r){return service.accessRequest(r.projectId(),r.requester(),r.role(),r.reason());}
    @PutMapping("/collaboration/access-requests/{id}") public Object accessDecision(@PathVariable UUID id,@RequestBody AccessDecisionRequest r){return service.accessDecision(id,r.approved(),r.actor());}
    @PostMapping("/collaboration/folders") public Object folder(@RequestBody FolderRequest r){return service.folder(r.name(),r.parentId());}
    @PostMapping("/collaboration/transfer") public Object transfer(@RequestBody TransferRequest r){return service.transfer(r.projectId(),r.fromOwner(),r.toOwner(),r.actor());}

    // P27 Extensibility
    @GetMapping("/extensibility") public Object extensibility(){return service.extensibility();}
    @PostMapping("/extensibility/tokens") public Object token(@RequestBody ApiTokenRequest r){return service.apiToken(r.name(),r.subject(),r.scopes(),r.expiresInDays());}
    @PostMapping("/extensibility/webhooks") public Object webhook(@RequestBody WebhookRequest r){return service.webhook(r.name(),r.url(),r.events(),r.secretRef());}
    @PostMapping("/extensibility/plugins") public Object plugin(@RequestBody PluginRequest r){return service.plugin(r.id(),r.name(),r.type(),r.endpoint(),r.scopes());}
    @PutMapping("/extensibility/rate-limits/{subject}") public Object rateLimit(@PathVariable String subject,@RequestBody RateLimitRequest r){return service.rateLimit(subject,r.requestsPerMinute(),r.burst());}

    // P28 FinOps
    @GetMapping("/finops") public Object finops(){return service.finops();}
    @PutMapping("/finops/budgets/{scope}") public Object budget(@PathVariable String scope,@RequestBody BudgetRequest r){return service.budget(scope,r.monthlyLimit(),r.alertPercent(),r.costCenter());}
    @PostMapping("/finops/usage") public Object usage(@RequestBody UsageRequest r){return service.usage(r.scope(),r.category(),r.units(),r.unitCost(),r.model());}
    @PutMapping("/finops/model-policy/{scope}") public Object modelPolicy(@PathVariable String scope,@RequestBody ModelPolicyRequest r){return service.modelPolicy(scope,r.primary(),r.fallback(),r.maxCostPerRequest());}
    @GetMapping("/finops/recommendations") public Object recommendations(){return service.recommendations();}

    // P29 Reliability
    @GetMapping("/reliability") public Object reliability(){return service.reliability();}
    @PutMapping("/reliability/slos/{name}") public Object slo(@PathVariable String name,@RequestBody SloRequest r){return service.slo(name,r.target(),r.windowDays(),r.metric());}
    @PostMapping("/reliability/incidents") public Object incident(@RequestBody IncidentRequest r){return service.incident(r.title(),r.severity(),r.owner(),r.message());}
    @PutMapping("/reliability/incidents/{id}") public Object incidentUpdate(@PathVariable UUID id,@RequestBody IncidentUpdateRequest r){return service.incidentUpdate(id,r.status(),r.message(),r.actor());}
    @PostMapping("/reliability/drills") public Object drill(@RequestBody DrillRequest r){return service.drill(r.type(),r.scope(),r.passed(),r.evidence(),r.actor());}
    @PutMapping("/reliability/flags/{key}") public Object flag(@PathVariable String key,@RequestBody FlagRequest r){return service.flag(key,r.enabled(),r.rolloutPercent(),r.actor());}
    @GetMapping("/reliability/diagnostics") public Object diagnostics(){return service.diagnostics();}

    public record ActorRequest(String actor){}
    public record AgentJobRequest(UUID projectId,String prompt,int maxRetries,long timeoutSeconds,boolean approvalRequired){}
    public record AgentStepRequest(String phase,String message,double costUnits){}
    public record McpRequest(String name,String endpoint,String scope,boolean enabled){}
    public record DataEnvironmentRequest(UUID projectId,String name,String region,long quotaMb){}
    public record BackupRequest(UUID projectId,String environment,String type){}
    public record ClassificationRequest(UUID projectId,String resource,String classification,boolean pii){}
    public record MigrationRequest(UUID projectId,String environment,String version,String description,boolean safe){}
    public record RuntimeLimitRequest(int cpuMillis,int memoryMb,int timeoutSeconds,String networkMode){}
    public record ArtifactRequest(UUID projectId,String environment,String digest,String sourceVersion){}
    public record DeploymentRequest(UUID projectId,String environment,UUID artifactId,String strategy){}
    public record DomainRequest(UUID projectId,String domain,String verificationToken){}
    public record DomainVerifyRequest(String observedToken){}
    public record PresenceRequest(String user,UUID projectId,String resource){}
    public record ThreadRequest(UUID projectId,String target,String author,String body,List<String> mentions){}
    public record AccessRequest(UUID projectId,String requester,String role,String reason){}
    public record AccessDecisionRequest(boolean approved,String actor){}
    public record FolderRequest(String name,UUID parentId){}
    public record TransferRequest(UUID projectId,String fromOwner,String toOwner,String actor){}
    public record ApiTokenRequest(String name,String subject,List<String> scopes,int expiresInDays){}
    public record WebhookRequest(String name,String url,List<String> events,String secretRef){}
    public record PluginRequest(String id,String name,String type,String endpoint,List<String> scopes){}
    public record RateLimitRequest(int requestsPerMinute,int burst){}
    public record BudgetRequest(double monthlyLimit,double alertPercent,String costCenter){}
    public record UsageRequest(String scope,String category,double units,double unitCost,String model){}
    public record ModelPolicyRequest(String primary,String fallback,double maxCostPerRequest){}
    public record SloRequest(double target,int windowDays,String metric){}
    public record IncidentRequest(String title,String severity,String owner,String message){}
    public record IncidentUpdateRequest(String status,String message,String actor){}
    public record DrillRequest(String type,String scope,boolean passed,String evidence,String actor){}
    public record FlagRequest(boolean enabled,int rolloutPercent,String actor){}
}

@Service
class P23P29EnterpriseRuntimeService {
    private static final String FILE="p23-p29-enterprise-runtime.json";
    private final PlatformStateStore store; private State state;
    P23P29EnterpriseRuntimeService(PlatformStateStore store){this.store=store;this.state=store.read(FILE,new TypeReference<State>(){},State::empty);}

    synchronized List<AgentJob> jobs(){return List.copyOf(state.jobs());}
    synchronized AgentJob enqueue(UUID projectId,String prompt,int retries,long timeout,boolean approval){AgentJob j=new AgentJob(UUID.randomUUID(),projectId,prompt,"PLANNED",Math.max(0,Math.min(retries,5)),Math.max(30,Math.min(timeout<=0?900:timeout,7200)),approval,approval?"WAITING_APPROVAL":"QUEUED",0,0,Instant.now(),Instant.now(),new ArrayList<>());state.jobs().add(0,j);persist();return j;}
    synchronized AgentJob agentStep(UUID id,String phase,String message,double cost){AgentJob j=job(id);if("CANCELLED".equals(j.status()))return j;List<TraceStep> trace=new ArrayList<>(j.trace());trace.add(new TraceStep(Instant.now(),phase,message,cost));String next=switch(String.valueOf(phase).toUpperCase(Locale.ROOT)){case "PLAN"->"PLANNED";case "EXECUTE"->"RUNNING";case "VERIFY"->"VERIFYING";case "COMPLETE"->"SUCCEEDED";case "FAIL"->"FAILED";default->j.status();};AgentJob u=new AgentJob(j.id(),j.projectId(),j.prompt(),phase,j.maxRetries(),j.timeoutSeconds(),j.approvalRequired(),next,j.attempts()+("EXECUTE".equalsIgnoreCase(phase)?1:0),j.costUnits()+Math.max(0,cost),j.createdAt(),Instant.now(),List.copyOf(trace));replaceJob(u);return u;}
    synchronized AgentJob approveJob(UUID id,String actor){AgentJob j=job(id);AgentJob u=new AgentJob(j.id(),j.projectId(),j.prompt(),j.phase(),j.maxRetries(),j.timeoutSeconds(),j.approvalRequired(),"QUEUED",j.attempts(),j.costUnits(),j.createdAt(),Instant.now(),append(j.trace(),new TraceStep(Instant.now(),"APPROVAL","Approved by "+actor,0)));replaceJob(u);return u;}
    synchronized AgentJob cancelJob(UUID id,String actor){AgentJob j=job(id);AgentJob u=new AgentJob(j.id(),j.projectId(),j.prompt(),j.phase(),j.maxRetries(),j.timeoutSeconds(),j.approvalRequired(),"CANCELLED",j.attempts(),j.costUnits(),j.createdAt(),Instant.now(),append(j.trace(),new TraceStep(Instant.now(),"CANCEL","Cancelled by "+actor,0)));replaceJob(u);return u;}
    synchronized McpEndpoint registerMcp(String name,String endpoint,String scope,boolean enabled){McpEndpoint m=new McpEndpoint(UUID.randomUUID(),name,endpoint,scope,enabled,Instant.now());state.mcp().add(m);persist();return m;}

    synchronized DataView data(){return new DataView(List.copyOf(state.dataEnvironments()),List.copyOf(state.backups()),List.copyOf(state.classifications()),List.copyOf(state.migrations()));}
    synchronized DataEnvironment environment(UUID projectId,String name,String region,long quota){DataEnvironment e=new DataEnvironment(UUID.randomUUID(),projectId,name,region==null?"default":region,Math.max(128,quota<=0?1024:quota),"READY",Instant.now());state.dataEnvironments().add(e);persist();return e;}
    synchronized Backup backup(UUID projectId,String environment,String type){Backup b=new Backup(UUID.randomUUID(),projectId,environment,type==null?"SNAPSHOT":type.toUpperCase(Locale.ROOT),"CREATED",null,Instant.now(),null);state.backups().add(0,b);persist();return b;}
    synchronized Backup restoreTest(UUID id,String actor){for(int i=0;i<state.backups().size();i++){Backup b=state.backups().get(i);if(b.id().equals(id)){Backup u=new Backup(b.id(),b.projectId(),b.environment(),b.type(),"RESTORE_TEST_PASSED",actor,b.createdAt(),Instant.now());state.backups().set(i,u);persist();return u;}}throw new IllegalArgumentException("Backup not found");}
    synchronized DataClassification classify(UUID projectId,String resource,String classification,boolean pii){DataClassification c=new DataClassification(UUID.randomUUID(),projectId,resource,classification,pii,Instant.now());state.classifications().add(c);persist();return c;}
    synchronized Migration migration(UUID projectId,String environment,String version,String description,boolean safe){Migration m=new Migration(UUID.randomUUID(),projectId,environment,version,description,safe,safe?"APPROVED":"REQUIRES_REVIEW",Instant.now());state.migrations().add(m);persist();return m;}

    synchronized DeploymentView deployments(){return new DeploymentView(Map.copyOf(state.runtimeLimits()),List.copyOf(state.artifacts()),List.copyOf(state.deployments()),List.copyOf(state.domains()));}
    synchronized RuntimeLimits limits(UUID projectId,int cpu,int memory,int timeout,String network){RuntimeLimits l=new RuntimeLimits(projectId,Math.max(100,cpu),Math.max(128,memory),Math.max(30,timeout),network==null?"RESTRICTED":network,Instant.now());state.runtimeLimits().put(projectId,l);persist();return l;}
    synchronized Artifact artifact(UUID projectId,String env,String digest,String version){Artifact a=new Artifact(UUID.randomUUID(),projectId,env,digest,version,Instant.now());state.artifacts().add(0,a);persist();return a;}
    synchronized Deployment deploy(UUID projectId,String env,UUID artifactId,String strategy){artifactById(artifactId);Deployment d=new Deployment(UUID.randomUUID(),projectId,env,artifactId,strategy==null?"IMMUTABLE":strategy.toUpperCase(Locale.ROOT),"DEPLOYED",null,Instant.now(),Instant.now());state.deployments().add(0,d);persist();return d;}
    synchronized Deployment rollback(UUID id,String actor){for(int i=0;i<state.deployments().size();i++){Deployment d=state.deployments().get(i);if(d.id().equals(id)){Deployment u=new Deployment(d.id(),d.projectId(),d.environment(),d.artifactId(),d.strategy(),"ROLLED_BACK",actor,d.createdAt(),Instant.now());state.deployments().set(i,u);persist();return u;}}throw new IllegalArgumentException("Deployment not found");}
    synchronized CustomDomain domain(UUID projectId,String domain,String token){CustomDomain d=new CustomDomain(UUID.randomUUID(),projectId,domain,token,"PENDING_DNS",false,Instant.now());state.domains().add(d);persist();return d;}
    synchronized CustomDomain verifyDomain(UUID id,String observed){for(int i=0;i<state.domains().size();i++){CustomDomain d=state.domains().get(i);if(d.id().equals(id)){boolean ok=Objects.equals(d.verificationToken(),observed);CustomDomain u=new CustomDomain(d.id(),d.projectId(),d.domain(),d.verificationToken(),ok?"TLS_READY":"DNS_MISMATCH",ok,Instant.now());state.domains().set(i,u);persist();return u;}}throw new IllegalArgumentException("Domain not found");}

    synchronized CollaborationView collaboration(){return new CollaborationView(Map.copyOf(state.presence()),List.copyOf(state.threads()),List.copyOf(state.accessRequests()),List.copyOf(state.folders()),List.copyOf(state.transfers()));}
    synchronized Presence presence(String user,UUID projectId,String resource){Presence p=new Presence(user,projectId,resource,Instant.now());state.presence().put(user,p);persist();return p;}
    synchronized ThreadItem thread(UUID projectId,String target,String author,String body,List<String> mentions){ThreadItem t=new ThreadItem(UUID.randomUUID(),projectId,target,author,body,mentions==null?List.of():List.copyOf(mentions),"OPEN",Instant.now());state.threads().add(0,t);for(String m:t.mentions())state.notifications().add(0,new Notification(UUID.randomUUID(),m,"MENTION",projectId.toString(),false,Instant.now()));persist();return t;}
    synchronized AccessRequest accessRequest(UUID projectId,String requester,String role,String reason){AccessRequest a=new AccessRequest(UUID.randomUUID(),projectId,requester,role,reason,"PENDING",null,Instant.now(),Instant.now());state.accessRequests().add(0,a);persist();return a;}
    synchronized AccessRequest accessDecision(UUID id,boolean approved,String actor){for(int i=0;i<state.accessRequests().size();i++){AccessRequest a=state.accessRequests().get(i);if(a.id().equals(id)){AccessRequest u=new AccessRequest(a.id(),a.projectId(),a.requester(),a.role(),a.reason(),approved?"APPROVED":"REJECTED",actor,a.createdAt(),Instant.now());state.accessRequests().set(i,u);state.notifications().add(0,new Notification(UUID.randomUUID(),a.requester(),"ACCESS_"+u.status(),a.projectId().toString(),false,Instant.now()));persist();return u;}}throw new IllegalArgumentException("Access request not found");}
    synchronized Folder folder(String name,UUID parent){Folder f=new Folder(UUID.randomUUID(),name,parent,Instant.now());state.folders().add(f);persist();return f;}
    synchronized Transfer transfer(UUID projectId,String from,String to,String actor){Transfer t=new Transfer(UUID.randomUUID(),projectId,from,to,actor,Instant.now());state.transfers().add(0,t);persist();return t;}

    synchronized ExtensibilityView extensibility(){return new ExtensibilityView(List.copyOf(state.apiTokens()),List.copyOf(state.webhooks()),Map.copyOf(state.plugins()),Map.copyOf(state.rateLimits()),List.copyOf(state.mcp()));}
    synchronized ApiToken apiToken(String name,String subject,List<String> scopes,int days){String raw=UUID.randomUUID().toString().replace("-","")+UUID.randomUUID().toString().replace("-","");String prefix=raw.substring(0,10);ApiToken t=new ApiToken(UUID.randomUUID(),name,subject,prefix,scopes==null?List.of():List.copyOf(scopes),Instant.now(),Instant.now().plus(Duration.ofDays(Math.max(1,Math.min(days<=0?90:days,365)))));state.apiTokens().add(t);persist();return t;}
    synchronized Webhook webhook(String name,String url,List<String> events,String secretRef){Webhook w=new Webhook(UUID.randomUUID(),name,url,events==null?List.of():List.copyOf(events),secretRef,true,Instant.now());state.webhooks().add(w);persist();return w;}
    synchronized Plugin plugin(String id,String name,String type,String endpoint,List<String> scopes){Plugin p=new Plugin(id,name,type,endpoint,scopes==null?List.of():List.copyOf(scopes),true,Instant.now());state.plugins().put(id,p);persist();return p;}
    synchronized RateLimit rateLimit(String subject,int rpm,int burst){RateLimit r=new RateLimit(subject,Math.max(1,rpm),Math.max(1,burst),Instant.now());state.rateLimits().put(subject,r);persist();return r;}

    synchronized FinOpsView finops(){double month=state.usage().stream().filter(u->u.at().isAfter(Instant.now().minus(Duration.ofDays(30)))).mapToDouble(UsageEntry::cost).sum();return new FinOpsView(Map.copyOf(state.budgets()),List.copyOf(state.usage()),Map.copyOf(state.modelPolicies()),List.copyOf(state.alerts()),month);}
    synchronized Budget budget(String scope,double limit,double alert,String costCenter){Budget b=new Budget(scope,Math.max(0,limit),Math.max(1,Math.min(alert<=0?80:alert,100)),costCenter,Instant.now());state.budgets().put(scope,b);persist();return b;}
    synchronized UsageEntry usage(String scope,String category,double units,double unitCost,String model){UsageEntry u=new UsageEntry(UUID.randomUUID(),scope,category,Math.max(0,units),Math.max(0,unitCost),Math.max(0,units*unitCost),model,Instant.now());state.usage().add(0,u);Budget b=state.budgets().get(scope);if(b!=null){double spent=state.usage().stream().filter(x->scope.equals(x.scope())&&x.at().isAfter(Instant.now().minus(Duration.ofDays(30)))).mapToDouble(UsageEntry::cost).sum();if(b.monthlyLimit()>0&&spent>=b.monthlyLimit()*b.alertPercent()/100.0)state.alerts().add(0,new CostAlert(UUID.randomUUID(),scope,spent,b.monthlyLimit(),Instant.now()));}persist();return u;}
    synchronized ModelPolicy modelPolicy(String scope,String primary,String fallback,double max){ModelPolicy m=new ModelPolicy(scope,primary,fallback,Math.max(0,max),Instant.now());state.modelPolicies().put(scope,m);persist();return m;}
    synchronized List<String> recommendations(){List<String> r=new ArrayList<>();state.budgets().forEach((scope,b)->{double spent=state.usage().stream().filter(x->scope.equals(x.scope())&&x.at().isAfter(Instant.now().minus(Duration.ofDays(30)))).mapToDouble(UsageEntry::cost).sum();if(b.monthlyLimit()>0&&spent>b.monthlyLimit()*.8)r.add(scope+": route low-risk requests to the configured fallback model and review runtime/tool usage");});if(r.isEmpty())r.add("No budget pressure detected; continue monitoring token, tool, runtime and storage consumption.");return List.copyOf(r);}

    synchronized ReliabilityView reliability(){return new ReliabilityView(Map.copyOf(state.slos()),List.copyOf(state.incidents()),List.copyOf(state.drills()),Map.copyOf(state.flags()));}
    synchronized Slo slo(String name,double target,int days,String metric){Slo s=new Slo(name,Math.max(0,Math.min(target,100)),Math.max(1,days),metric,Instant.now());state.slos().put(name,s);persist();return s;}
    synchronized Incident incident(String title,String severity,String owner,String message){Incident i=new Incident(UUID.randomUUID(),title,severity,owner,"OPEN",List.of(new IncidentEvent(Instant.now(),"system",message)),Instant.now(),Instant.now());state.incidents().add(0,i);persist();return i;}
    synchronized Incident incidentUpdate(UUID id,String status,String message,String actor){for(int i=0;i<state.incidents().size();i++){Incident x=state.incidents().get(i);if(x.id().equals(id)){List<IncidentEvent> events=new ArrayList<>(x.events());events.add(new IncidentEvent(Instant.now(),actor,message));Incident u=new Incident(x.id(),x.title(),x.severity(),x.owner(),status==null?x.status():status.toUpperCase(Locale.ROOT),List.copyOf(events),x.createdAt(),Instant.now());state.incidents().set(i,u);persist();return u;}}throw new IllegalArgumentException("Incident not found");}
    synchronized Drill drill(String type,String scope,boolean passed,String evidence,String actor){Drill d=new Drill(UUID.randomUUID(),type,scope,passed,evidence,actor,Instant.now());state.drills().add(0,d);persist();return d;}
    synchronized FeatureFlag flag(String key,boolean enabled,int percent,String actor){FeatureFlag f=new FeatureFlag(key,enabled,Math.max(0,Math.min(percent,100)),actor,Instant.now());state.flags().put(key,f);persist();return f;}
    synchronized Diagnostics diagnostics(){long open=state.incidents().stream().filter(i->!"RESOLVED".equals(i.status())).count();long passed=state.drills().stream().filter(Drill::passed).count();return new Diagnostics(open,state.drills().size(),passed,state.jobs().size(),state.deployments().size(),state.backups().size(),Instant.now());}

    private AgentJob job(UUID id){return state.jobs().stream().filter(j->j.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Agent job not found"));}
    private void replaceJob(AgentJob u){for(int i=0;i<state.jobs().size();i++)if(state.jobs().get(i).id().equals(u.id())){state.jobs().set(i,u);persist();return;}}
    private List<TraceStep> append(List<TraceStep> list,TraceStep v){List<TraceStep> x=new ArrayList<>(list);x.add(v);return List.copyOf(x);}
    private Artifact artifactById(UUID id){return state.artifacts().stream().filter(a->a.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Artifact not found"));}
    private void persist(){store.write(FILE,state);}

    record TraceStep(Instant at,String phase,String message,double costUnits){}
    record AgentJob(UUID id,UUID projectId,String prompt,String phase,int maxRetries,long timeoutSeconds,boolean approvalRequired,String status,int attempts,double costUnits,Instant createdAt,Instant updatedAt,List<TraceStep> trace){}
    record McpEndpoint(UUID id,String name,String endpoint,String scope,boolean enabled,Instant updatedAt){}
    record DataEnvironment(UUID id,UUID projectId,String name,String region,long quotaMb,String status,Instant createdAt){}
    record Backup(UUID id,UUID projectId,String environment,String type,String status,String testedBy,Instant createdAt,Instant testedAt){}
    record DataClassification(UUID id,UUID projectId,String resource,String classification,boolean pii,Instant createdAt){}
    record Migration(UUID id,UUID projectId,String environment,String version,String description,boolean safe,String status,Instant createdAt){}
    record DataView(List<DataEnvironment> environments,List<Backup> backups,List<DataClassification> classifications,List<Migration> migrations){}
    record RuntimeLimits(UUID projectId,int cpuMillis,int memoryMb,int timeoutSeconds,String networkMode,Instant updatedAt){}
    record Artifact(UUID id,UUID projectId,String environment,String digest,String sourceVersion,Instant createdAt){}
    record Deployment(UUID id,UUID projectId,String environment,UUID artifactId,String strategy,String status,String actor,Instant createdAt,Instant updatedAt){}
    record CustomDomain(UUID id,UUID projectId,String domain,String verificationToken,String status,boolean tlsReady,Instant updatedAt){}
    record DeploymentView(Map<UUID,RuntimeLimits> limits,List<Artifact> artifacts,List<Deployment> deployments,List<CustomDomain> domains){}
    record Presence(String user,UUID projectId,String resource,Instant seenAt){}
    record ThreadItem(UUID id,UUID projectId,String target,String author,String body,List<String> mentions,String status,Instant createdAt){}
    record Notification(UUID id,String user,String type,String target,boolean read,Instant createdAt){}
    record AccessRequest(UUID id,UUID projectId,String requester,String role,String reason,String status,String actor,Instant createdAt,Instant updatedAt){}
    record Folder(UUID id,String name,UUID parentId,Instant createdAt){}
    record Transfer(UUID id,UUID projectId,String fromOwner,String toOwner,String actor,Instant createdAt){}
    record CollaborationView(Map<String,Presence> presence,List<ThreadItem> threads,List<AccessRequest> accessRequests,List<Folder> folders,List<Transfer> transfers){}
    record ApiToken(UUID id,String name,String subject,String tokenPrefix,List<String> scopes,Instant createdAt,Instant expiresAt){}
    record Webhook(UUID id,String name,String url,List<String> events,String secretRef,boolean enabled,Instant createdAt){}
    record Plugin(String id,String name,String type,String endpoint,List<String> scopes,boolean enabled,Instant updatedAt){}
    record RateLimit(String subject,int requestsPerMinute,int burst,Instant updatedAt){}
    record ExtensibilityView(List<ApiToken> tokens,List<Webhook> webhooks,Map<String,Plugin> plugins,Map<String,RateLimit> rateLimits,List<McpEndpoint> mcp){}
    record Budget(String scope,double monthlyLimit,double alertPercent,String costCenter,Instant updatedAt){}
    record UsageEntry(UUID id,String scope,String category,double units,double unitCost,double cost,String model,Instant at){}
    record ModelPolicy(String scope,String primary,String fallback,double maxCostPerRequest,Instant updatedAt){}
    record CostAlert(UUID id,String scope,double spent,double budget,Instant at){}
    record FinOpsView(Map<String,Budget> budgets,List<UsageEntry> usage,Map<String,ModelPolicy> modelPolicies,List<CostAlert> alerts,double last30DayCost){}
    record Slo(String name,double target,int windowDays,String metric,Instant updatedAt){}
    record IncidentEvent(Instant at,String actor,String message){}
    record Incident(UUID id,String title,String severity,String owner,String status,List<IncidentEvent> events,Instant createdAt,Instant updatedAt){}
    record Drill(UUID id,String type,String scope,boolean passed,String evidence,String actor,Instant createdAt){}
    record FeatureFlag(String key,boolean enabled,int rolloutPercent,String actor,Instant updatedAt){}
    record ReliabilityView(Map<String,Slo> slos,List<Incident> incidents,List<Drill> drills,Map<String,FeatureFlag> flags){}
    record Diagnostics(long openIncidents,long drills,long passedDrills,long agentJobs,long deployments,long backups,Instant generatedAt){}

    static final class State {
        private final List<AgentJob> jobs;private final List<McpEndpoint> mcp;private final List<DataEnvironment> dataEnvironments;private final List<Backup> backups;private final List<DataClassification> classifications;private final List<Migration> migrations;private final Map<UUID,RuntimeLimits> runtimeLimits;private final List<Artifact> artifacts;private final List<Deployment> deployments;private final List<CustomDomain> domains;private final Map<String,Presence> presence;private final List<ThreadItem> threads;private final List<Notification> notifications;private final List<AccessRequest> accessRequests;private final List<Folder> folders;private final List<Transfer> transfers;private final List<ApiToken> apiTokens;private final List<Webhook> webhooks;private final Map<String,Plugin> plugins;private final Map<String,RateLimit> rateLimits;private final Map<String,Budget> budgets;private final List<UsageEntry> usage;private final Map<String,ModelPolicy> modelPolicies;private final List<CostAlert> alerts;private final Map<String,Slo> slos;private final List<Incident> incidents;private final List<Drill> drills;private final Map<String,FeatureFlag> flags;
        State(List<AgentJob> jobs,List<McpEndpoint> mcp,List<DataEnvironment> dataEnvironments,List<Backup> backups,List<DataClassification> classifications,List<Migration> migrations,Map<UUID,RuntimeLimits> runtimeLimits,List<Artifact> artifacts,List<Deployment> deployments,List<CustomDomain> domains,Map<String,Presence> presence,List<ThreadItem> threads,List<Notification> notifications,List<AccessRequest> accessRequests,List<Folder> folders,List<Transfer> transfers,List<ApiToken> apiTokens,List<Webhook> webhooks,Map<String,Plugin> plugins,Map<String,RateLimit> rateLimits,Map<String,Budget> budgets,List<UsageEntry> usage,Map<String,ModelPolicy> modelPolicies,List<CostAlert> alerts,Map<String,Slo> slos,List<Incident> incidents,List<Drill> drills,Map<String,FeatureFlag> flags){this.jobs=jobs;this.mcp=mcp;this.dataEnvironments=dataEnvironments;this.backups=backups;this.classifications=classifications;this.migrations=migrations;this.runtimeLimits=runtimeLimits;this.artifacts=artifacts;this.deployments=deployments;this.domains=domains;this.presence=presence;this.threads=threads;this.notifications=notifications;this.accessRequests=accessRequests;this.folders=folders;this.transfers=transfers;this.apiTokens=apiTokens;this.webhooks=webhooks;this.plugins=plugins;this.rateLimits=rateLimits;this.budgets=budgets;this.usage=usage;this.modelPolicies=modelPolicies;this.alerts=alerts;this.slos=slos;this.incidents=incidents;this.drills=drills;this.flags=flags;}
        static State empty(){return new State(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new LinkedHashMap<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new LinkedHashMap<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new ArrayList<>(),new LinkedHashMap<>(),new ArrayList<>(),new LinkedHashMap<>(),new ArrayList<>(),new ArrayList<>(),new LinkedHashMap<>());}
        List<AgentJob> jobs(){return jobs;}List<McpEndpoint> mcp(){return mcp;}List<DataEnvironment> dataEnvironments(){return dataEnvironments;}List<Backup> backups(){return backups;}List<DataClassification> classifications(){return classifications;}List<Migration> migrations(){return migrations;}Map<UUID,RuntimeLimits> runtimeLimits(){return runtimeLimits;}List<Artifact> artifacts(){return artifacts;}List<Deployment> deployments(){return deployments;}List<CustomDomain> domains(){return domains;}Map<String,Presence> presence(){return presence;}List<ThreadItem> threads(){return threads;}List<Notification> notifications(){return notifications;}List<AccessRequest> accessRequests(){return accessRequests;}List<Folder> folders(){return folders;}List<Transfer> transfers(){return transfers;}List<ApiToken> apiTokens(){return apiTokens;}List<Webhook> webhooks(){return webhooks;}Map<String,Plugin> plugins(){return plugins;}Map<String,RateLimit> rateLimits(){return rateLimits;}Map<String,Budget> budgets(){return budgets;}List<UsageEntry> usage(){return usage;}Map<String,ModelPolicy> modelPolicies(){return modelPolicies;}List<CostAlert> alerts(){return alerts;}Map<String,Slo> slos(){return slos;}List<Incident> incidents(){return incidents;}List<Drill> drills(){return drills;}Map<String,FeatureFlag> flags(){return flags;}
    }
}