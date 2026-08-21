package io.forgepilot.enterprise;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseGovernanceController {
    private final EnterpriseGovernanceService service;
    public EnterpriseGovernanceController(EnterpriseGovernanceService service){this.service=service;}

    @GetMapping("/settings") public EnterpriseGovernanceService.EnterpriseSettings settings(){return service.settings();}
    @PutMapping("/identity") public EnterpriseGovernanceService.EnterpriseSettings identity(@RequestBody EnterpriseGovernanceService.IdentityConfig request){return service.updateIdentity(request);}
    @PutMapping("/scim") public EnterpriseGovernanceService.EnterpriseSettings scim(@RequestBody EnterpriseGovernanceService.ScimConfig request){return service.updateScim(request);}
    @PutMapping("/policy") public EnterpriseGovernanceService.EnterpriseSettings policy(@RequestBody EnterpriseGovernanceService.GovernancePolicy request){return service.updatePolicy(request);}
    @PutMapping("/brand") public EnterpriseGovernanceService.EnterpriseSettings brand(@RequestBody EnterpriseGovernanceService.BrandPolicy request){return service.updateBrand(request);}
    @PutMapping("/support") public EnterpriseGovernanceService.EnterpriseSettings support(@RequestBody EnterpriseGovernanceService.SupportProfile request){return service.updateSupport(request);}
    @GetMapping("/audit") public List<EnterpriseGovernanceService.AuditEvent> audit(){return service.audit();}
    @GetMapping("/insights") public EnterpriseGovernanceService.WorkspaceInsights insights(){return service.insights();}
}
