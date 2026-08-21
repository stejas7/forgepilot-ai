package io.forgepilot.enterprise;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.parity.ParityAuditService;
import io.forgepilot.platform.PlatformStateStore;
import io.forgepilot.readiness.ProductionReadinessService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise/acceptance")
public class P30AcceptanceController {
    private final P30AcceptanceService service;
    public P30AcceptanceController(P30AcceptanceService service){this.service=service;}

    @GetMapping public P30AcceptanceService.AcceptanceReport report(){return service.report();}
    @PutMapping("/evidence/{check}") public P30AcceptanceService.Evidence evidence(@PathVariable String check,@RequestBody EvidenceRequest r){return service.record(check,r.status(),r.reference(),r.detail(),r.actor());}
    @PostMapping("/run") public P30AcceptanceService.AcceptanceReport run(@RequestBody(required=false) RunRequest r){return service.run(r==null?"system":r.actor());}
    @GetMapping("/matrix") public List<P30AcceptanceService.AcceptanceCheck> matrix(){return service.report().checks();}

    public record EvidenceRequest(String status,String reference,String detail,String actor){}
    public record RunRequest(String actor){}
}

@Service
class P30AcceptanceService {
    private static final String FILE="p30-acceptance.json";
    private static final List<String> REQUIRED=List.of(
            "CREATOR_E2E","IDENTITY_SCIM","RBAC_PUBLISHING","CONNECTOR_PERMISSIONS","GITHUB_SYNC",
            "SECURITY_PUBLISH_GATE","INSIGHTS_AUDIT_TRUST","PERFORMANCE_RESILIENCE","BACKUP_RESTORE_ROLLBACK",
            "ACCESSIBILITY_RESPONSIVE","DEAD_CODE_CLEANUP","DOCS_ALIGNMENT","CI_CD_DEPLOYMENT");
    private final PlatformStateStore store;private final ParityAuditService parity;private final ProductionReadinessService readiness;private State state;
    P30AcceptanceService(PlatformStateStore store,ParityAuditService parity,ProductionReadinessService readiness){this.store=store;this.parity=parity;this.readiness=readiness;this.state=store.read(FILE,new TypeReference<State>(){},State::empty);}

    synchronized Evidence record(String check,String status,String reference,String detail,String actor){String key=normalize(check);if(!REQUIRED.contains(key))throw new IllegalArgumentException("Unknown acceptance check: "+key);String s=status==null?"PARTIAL":status.toUpperCase(Locale.ROOT);if(!List.of("PASS","PARTIAL","GAP").contains(s))throw new IllegalArgumentException("status must be PASS, PARTIAL or GAP");Evidence e=new Evidence(key,s,reference,detail,actor,Instant.now());state.evidence().put(key,e);state.runs().add(0,new RunEvent(Instant.now(),actor,"EVIDENCE",key+"="+s));persist();return e;}
    synchronized AcceptanceReport run(String actor){state.runs().add(0,new RunEvent(Instant.now(),actor,"ACCEPTANCE_RUN","P14/P15 and evidence matrix evaluated"));persist();return report();}
    synchronized AcceptanceReport report(){
        ParityAuditService.AuditReport p=parity.audit();ProductionReadinessService.ReadinessReport r=readiness.evaluate();List<AcceptanceCheck> checks=new ArrayList<>();
        for(String key:REQUIRED){Evidence e=state.evidence().get(key);checks.add(new AcceptanceCheck(key,e==null?"GAP":e.status(),e==null?"No evidence recorded":e.detail(),e==null?null:e.reference(),e==null?null:e.updatedAt()));}
        long pass=checks.stream().filter(c->"PASS".equals(c.status())).count();long partial=checks.stream().filter(c->"PARTIAL".equals(c.status())).count();long gap=checks.stream().filter(c->"GAP".equals(c.status())).count();
        boolean parityNoGap=p.gap()==0;boolean evidenceNoGap=gap==0;boolean accepted=evidenceNoGap&&parityNoGap;
        String status=accepted?(partial>0||p.partial()>0?"READY_WITH_ACCEPTED_PARTIALS":"ACCEPTED"):"NOT_ACCEPTED";
        return new AcceptanceReport(Instant.now(),status,pass,partial,gap,p.overallStatus(),p.partial(),p.gap(),r.status(),List.copyOf(checks),List.copyOf(state.runs()));
    }
    private String normalize(String v){return v==null?"":v.trim().toUpperCase(Locale.ROOT).replace('-','_');}
    private void persist(){store.write(FILE,state);}
    record Evidence(String check,String status,String reference,String detail,String actor,Instant updatedAt){}
    record RunEvent(Instant at,String actor,String action,String detail){}
    record AcceptanceCheck(String name,String status,String detail,String reference,Instant updatedAt){}
    record AcceptanceReport(Instant generatedAt,String status,long pass,long partial,long gap,String parityStatus,long parityPartial,long parityGap,String productionReadiness,List<AcceptanceCheck> checks,List<RunEvent> runHistory){}
    record State(Map<String,Evidence> evidence,List<RunEvent> runs){static State empty(){return new State(new LinkedHashMap<>(),new ArrayList<>());}}
}