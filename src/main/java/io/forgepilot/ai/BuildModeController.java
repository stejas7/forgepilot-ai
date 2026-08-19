package io.forgepilot.ai;

import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Initial Plan/Build command surface for ForgePilot AI.
 * Model-provider orchestration will replace the deterministic planner in M2.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/ai")
public class BuildModeController {

    private final ProjectService projectService;

    public BuildModeController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/plan")
    public AgentResponse plan(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.PLANNING);
        return new AgentResponse(
                "PLAN",
                project.id(),
                request.prompt(),
                List.of(
                        "Analyze requested product behavior and actors",
                        "Define screens, domain model and API contracts",
                        "Define implementation tasks and acceptance checks",
                        "Wait for Build command before changing workspace files"),
                "Plan prepared. No project files changed.",
                Instant.now());
    }

    @PostMapping("/build")
    public AgentResponse build(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.BUILDING);
        return new AgentResponse(
                "BUILD",
                project.id(),
                request.prompt(),
                List.of(
                        "Load current project context",
                        "Create an implementation change-set",
                        "Validate generated files in an isolated sandbox",
                        "Run tests and enter repair loop on failure",
                        "Create a version snapshot after validation"),
                "Build command accepted. Sandbox/code-generation runtime is the next implementation milestone.",
                Instant.now());
    }

    public record AgentRequest(UUID projectId, @NotBlank(message = "prompt is required") String prompt) {
    }

    public record AgentResponse(
            String mode,
            UUID projectId,
            String prompt,
            List<String> steps,
            String message,
            Instant createdAt) {
    }
}
