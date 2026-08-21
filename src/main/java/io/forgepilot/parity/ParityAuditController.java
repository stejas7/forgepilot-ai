package io.forgepilot.parity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parity")
public class ParityAuditController {
    private final ParityAuditService service;
    public ParityAuditController(ParityAuditService service){this.service=service;}
    @GetMapping("/audit") public ParityAuditService.AuditReport audit(){return service.audit();}
}
