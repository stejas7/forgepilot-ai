package io.forgepilot.enterprise;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/admin")
public class EnterpriseAdminController {
    private final EnterpriseAdminService admin;
    private final EnterpriseGovernanceService governance;
    public EnterpriseAdminController(EnterpriseAdminService admin,EnterpriseGovernanceService governance){this.admin=admin;this.governance=governance;}
    @GetMapping public EnterpriseAdminService.AdminDashboard dashboard(){return admin.dashboard();}
    @GetMapping("/audit") public Object audit(){return governance.audit();}
    @GetMapping("/settings") public EnterpriseGovernanceService.EnterpriseSettings settings(){return governance.settings();}
}
