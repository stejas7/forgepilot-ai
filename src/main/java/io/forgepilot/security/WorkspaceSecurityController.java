package io.forgepilot.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/security")
public class WorkspaceSecurityController {
    private final SecurityScanService service;
    public WorkspaceSecurityController(SecurityScanService service){this.service=service;}
    @GetMapping("/summary") public SecurityScanService.WorkspaceSecuritySummary summary(){return service.workspaceSummary();}
}
