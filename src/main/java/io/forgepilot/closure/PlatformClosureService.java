package io.forgepilot.closure;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P31-P40 control-plane read model. It exposes explicit evidence-oriented
 * milestone state and deliberately distinguishes implemented foundations from
 * capabilities that still require runtime/provider validation.
 *
 * @author Tejas Shah
 */
@Service
public class PlatformClosureService {

    public ClosureReport report() {
        Map<String, Milestone> milestones = new LinkedHashMap<>();
        milestones.put("P31", milestone("AI Model Gateway & Governance", List.of("provider registry", "allowlist policy", "routing/fallback", "cost metadata")));
        milestones.put("P32", milestone("AI Evaluation & Quality Engineering", List.of("evaluation suites", "regression evidence", "quality gate")));
        milestones.put("P33", milestone("Data Governance & Privacy", List.of("classification", "retention", "residency", "export/delete evidence")));
        milestones.put("P34", milestone("Workflow Automation & Eventing", List.of("workflow definitions", "triggers", "retry/dead-letter", "execution history")));
        milestones.put("P35", milestone("Extension Marketplace & Internal Catalog", List.of("catalog", "versioning", "approval", "install lifecycle")));
        milestones.put("P36", milestone("Mobile, PWA & Multi-device", List.of("PWA metadata", "device profiles", "responsive acceptance")));
        milestones.put("P37", milestone("Internationalization & Accessibility", List.of("locales", "translation lifecycle", "accessibility evidence", "release gate")));
        milestones.put("P38", milestone("Multi-region & Continuity", List.of("regions", "RPO/RTO", "DR evidence", "resilience gate")));
        milestones.put("P39", milestone("Executive Operations & Product Analytics", List.of("adoption", "funnel", "scorecards", "cost/value")));
        milestones.put("P40", new Milestone("Final Platform Closure & Acceptance", "PARTIAL",
                List.of("P14/P30/P31-P39 aggregation", "security/privacy/accessibility/resilience gates", "CI/deployment evidence"),
                "Acceptance remains PARTIAL until runtime regression and external/provider evidence are green."));
        return new ClosureReport(Instant.now(), "P31-P40", "IN_PROGRESS", milestones,
                "No milestone is promoted to PASS solely because its control-plane endpoint exists.");
    }

    public Map<String, Object> modelGateway() {
        return Map.of("providers", List.of("OPENAI"), "routingPolicy", "WORKSPACE_POLICY", "fallbackEnabled", true,
                "healthRequired", true, "costAccounting", true);
    }

    public Map<String, Object> evaluationGate() {
        return Map.of("required", true, "dimensions", List.of("QUALITY", "LATENCY", "COST", "SAFETY"),
                "releaseBlocking", true, "status", "EVIDENCE_REQUIRED");
    }

    public Map<String, Object> privacyGate() {
        return Map.of("classificationRequired", true, "retentionPolicyRequired", true, "residencyTracked", true,
                "exportDeleteEvidenceRequired", true, "status", "EVIDENCE_REQUIRED");
    }

    public Map<String, Object> resilienceGate() {
        return Map.of("rpoTargetMinutes", 60, "rtoTargetMinutes", 240, "drillEvidenceRequired", true,
                "multiRegionRuntime", "PARTIAL", "status", "EVIDENCE_REQUIRED");
    }

    private Milestone milestone(String name, List<String> capabilities) {
        return new Milestone(name, "FOUNDATION", capabilities,
                "Control-plane foundation present; runtime/evidence validation is required before PASS.");
    }

    public record ClosureReport(Instant generatedAt, String scope, String status,
                                Map<String, Milestone> milestones, String acceptanceRule) {}
    public record Milestone(String name, String status, List<String> capabilities, String evidence) {}
}
