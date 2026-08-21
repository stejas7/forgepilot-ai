package io.forgepilot.closure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** P31-P40 evidence and governance endpoints. */
@RestController
@RequestMapping("/api/platform")
public class PlatformClosureController {
    private final PlatformClosureService service;

    public PlatformClosureController(PlatformClosureService service) {
        this.service = service;
    }

    @GetMapping("/closure")
    public PlatformClosureService.ClosureReport closure() { return service.report(); }

    @GetMapping("/ai-governance")
    public Map<String,Object> aiGovernance() { return service.modelGateway(); }

    @GetMapping("/evaluation-gate")
    public Map<String,Object> evaluationGate() { return service.evaluationGate(); }

    @GetMapping("/privacy-gate")
    public Map<String,Object> privacyGate() { return service.privacyGate(); }

    @GetMapping("/resilience-gate")
    public Map<String,Object> resilienceGate() { return service.resilienceGate(); }
}
