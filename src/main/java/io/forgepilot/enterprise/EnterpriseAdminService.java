package io.forgepilot.enterprise;

import io.forgepilot.collaboration.WorkspaceCollaborationService;
import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import io.forgepilot.security.SecurityScanService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** P13 enterprise administration read model for governance and workspace insights. */
@Service
public class EnterpriseAdminService {
    private final ProjectService projects;
    private final EnterpriseGovernanceService governance;
    private final WorkspaceCollaborationService collaboration;
    private final SecurityScanService security;

    public EnterpriseAdminService(ProjectService projects,EnterpriseGovernanceService governance,WorkspaceCollaborationService collaboration,SecurityScanService security){
        this.projects=projects;this.governance=governance;this.collaboration=collaboration;this.security=security;
    }

    public AdminDashboard dashboard(){
        List<Project> all=projects.findAll();
        long abandoned=all.stream().filter(p->Duration.between(p.updatedAt(),Instant.now()).toDays()>=30).count();
        long blocked=all.stream().filter(p->{SecurityScanService.GateDecision g=security.gate(p.id());return !g.allowed();}).count();
        long scanned=all.stream().filter(p->security.latest(p.id())!=null).count();
        return new AdminDashboard(all.size(),collaboration.state().members().size(),scanned,blocked,abandoned,
                governance.settings(),governance.insights(),all.stream().map(this::inventory).toList());
    }

    private AppInventory inventory(Project p){SecurityScanService.GateDecision gate=security.gate(p.id());return new AppInventory(p.id(),p.name(),p.status().name(),p.stack(),p.updatedAt(),security.latest(p.id())!=null,gate.allowed(),Duration.between(p.updatedAt(),Instant.now()).toDays()>=30);}

    public record AppInventory(java.util.UUID projectId,String name,String status,String stack,Instant updatedAt,boolean scanned,boolean publishGatePassed,boolean abandoned){}
    public record AdminDashboard(long projects,long members,long scannedProjects,long blockedProjects,long abandonedProjects,EnterpriseGovernanceService.EnterpriseSettings settings,EnterpriseGovernanceService.WorkspaceInsights insights,List<AppInventory> applications){}
}
