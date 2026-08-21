package io.forgepilot.security;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/security")
public class SecurityScanController {
    private final SecurityScanService service;
    public SecurityScanController(SecurityScanService service){this.service=service;}

    @PostMapping("/scan") public SecurityScanService.ScanReport scan(@PathVariable UUID projectId,@RequestBody(required=false) ScanRequest r){return service.scan(projectId,r==null?"BASIC":r.depth(),r==null?"system":r.actor());}
    @GetMapping("/scans") public List<SecurityScanService.ScanReport> history(@PathVariable UUID projectId){return service.history(projectId);}
    @GetMapping("/gate") public SecurityScanService.GateDecision gate(@PathVariable UUID projectId){return service.gate(projectId);}
    @GetMapping("/trust") public SecurityScanService.TrustSummary trust(@PathVariable UUID projectId){return service.trust(projectId);}
    @PostMapping("/findings/{findingId}/suggest-fix") public SecurityScanService.FixSuggestion fix(@PathVariable UUID projectId,@PathVariable UUID findingId){return service.suggestFix(projectId,findingId);}
    @PostMapping("/findings/{findingId}/resolve") public SecurityScanService.Finding resolve(@PathVariable UUID projectId,@PathVariable UUID findingId,@RequestBody(required=false) Actor r){return service.resolve(projectId,findingId,r==null?"system":r.actor());}

    public record ScanRequest(String depth,String actor){}
    public record Actor(String actor){}
}
