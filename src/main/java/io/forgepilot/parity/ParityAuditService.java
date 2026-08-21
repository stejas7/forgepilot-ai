package io.forgepilot.parity;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

/** P14 auditable product-capability matrix. A PARTIAL/GAP is intentionally visible. */
@Service
public class ParityAuditService {
    public AuditReport audit(){
        List<Capability> items=List.of(
                c("Dashboard","Creator dashboard, prompt, Build/Plan, projects/search","PASS"),
                c("AI conversation","Persistent conversation, attachments, streaming, plans","PASS"),
                c("Working preview","Workspace-backed preview/runtime verification","PARTIAL","Arbitrary dependency sandbox/runtime isolation still needs hardening"),
                c("Visual editing","DOM selection, style/text editing, responsive overrides","PASS"),
                c("Code and versions","Monaco, file CRUD, snapshots, diff/restore","PASS"),
                c("Backend","PostgreSQL/schema/auth/RBAC/data contracts","PARTIAL","Generated-app auth/storage/realtime breadth needs production hardening"),
                c("GitHub","Connect/create/push/pull/branch/conflict protection","PASS"),
                c("Connectors","Catalog, project connections, secret-safe configuration","PARTIAL","Provider-specific OAuth breadth remains"),
                c("Publish","Immutable releases, approval, share URL, rollback","PARTIAL","Custom DNS/TLS automation remains"),
                c("Collaboration","Members, project sharing, roles, comments/activity","PARTIAL","Realtime concurrent editing/presence remains"),
                c("Security","Basic/deep scans, remediation, trust, publish gate","PASS"),
                c("Knowledge/agent","Knowledge, retrieval, skills, templates, task queue","PARTIAL","Full MCP and autonomous multi-agent execution remains"),
                c("Enterprise","Governance, SSO/SCIM config, RBAC policy, audit, insights","PARTIAL","Live IdP/SCIM protocol execution and admin UI breadth remains")
        );
        long pass=items.stream().filter(x->"PASS".equals(x.status())).count();
        long partial=items.stream().filter(x->"PARTIAL".equals(x.status())).count();
        long gap=items.stream().filter(x->"GAP".equals(x.status())).count();
        return new AuditReport(Instant.now(),items,pass,partial,gap,gap==0&&partial==0?"PARITY_COMPLETE":"GAPS_REMAIN");
    }
    private Capability c(String area,String evidence,String status){return new Capability(area,evidence,status,"");}
    private Capability c(String area,String evidence,String status,String gap){return new Capability(area,evidence,status,gap);}
    public record Capability(String area,String evidence,String status,String remainingGap){}
    public record AuditReport(Instant generatedAt,List<Capability> capabilities,long pass,long partial,long gap,String overallStatus){}
}
