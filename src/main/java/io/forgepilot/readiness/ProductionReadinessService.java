package io.forgepilot.readiness;

import io.forgepilot.parity.ParityAuditService;
import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import io.forgepilot.security.SecurityScanService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** P15 final release-readiness gate. */
@Service
public class ProductionReadinessService {
    private final ParityAuditService parity;
    private final ProjectService projects;
    private final SecurityScanService security;

    public ProductionReadinessService(ParityAuditService parity,ProjectService projects,SecurityScanService security){this.parity=parity;this.projects=projects;this.security=security;}

    public ReadinessReport evaluate(){
        List<Check> checks=new ArrayList<>();
        ParityAuditService.AuditReport audit=parity.audit();
        checks.add(new Check("PARITY","PARITY_COMPLETE".equals(audit.overallStatus()),audit.overallStatus()+" ("+audit.partial()+" partial, "+audit.gap()+" gaps)"));
        List<Project> all=projects.findAll();
        long scanned=all.stream().filter(p->security.latest(p.id())!=null).count();
        long blocked=all.stream().filter(p->security.latest(p.id())!=null&&!security.gate(p.id()).allowed()).count();
        checks.add(new Check("SECURITY_COVERAGE",all.isEmpty()||scanned==all.size(),scanned+"/"+all.size()+" projects scanned"));
        checks.add(new Check("SECURITY_GATE",blocked==0,blocked+" scanned projects blocked"));
        checks.add(new Check("PROJECT_STATE",true,all.size()+" durable projects visible"));
        boolean ready=checks.stream().allMatch(Check::passed);
        return new ReadinessReport(Instant.now(),ready?"READY_FOR_GA":"NOT_READY",List.copyOf(checks),audit);
    }

    public record Check(String name,boolean passed,String detail){}
    public record ReadinessReport(Instant generatedAt,String status,List<Check> checks,ParityAuditService.AuditReport parity){}
}
