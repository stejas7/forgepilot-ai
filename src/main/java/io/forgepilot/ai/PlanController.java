package io.forgepilot.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/plan")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<PlanService.ProjectPlan> get(@PathVariable UUID projectId) {
        PlanService.ProjectPlan plan = planService.get(projectId);
        return plan == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(plan);
    }

    @PutMapping
    public PlanService.ProjectPlan update(@PathVariable UUID projectId, @RequestBody PlanUpdate request) {
        return planService.update(projectId, request.content());
    }

    @PostMapping("/approve")
    public PlanService.ProjectPlan approve(@PathVariable UUID projectId) {
        return planService.approve(projectId);
    }

    public record PlanUpdate(String content) {}
}
