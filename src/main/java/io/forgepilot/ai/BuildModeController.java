package io.forgepilot.ai;

import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Plan/Build command surface for ForgePilot AI.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/ai")
public class BuildModeController {

    private static final String PLAN_SYSTEM = """
            You are ForgePilot AI's senior product architect. Convert the user's request into a concise,
            implementation-ready application plan. Cover actors, screens, domain entities, API contracts,
            security, data model, acceptance criteria, and implementation order. Do not claim that files
            were changed. Return plain text with clear sections and practical implementation detail.
            """;

    private static final String BUILD_SYSTEM = """
            You are ForgePilot AI's senior full-stack engineer. Produce a concise implementation change-set
            for the requested application using React/TypeScript for UI and Java 21/Spring Boot for backend
            unless the project context specifies otherwise. Describe files to create or update, important
            code responsibilities, APIs, data model, tests, security and validation. Do not claim execution
            occurred unless the platform confirms it. Return plain text suitable for an engineering build log.
            """;

    private final ProjectService projectService;
    private final OpenAiGateway openAiGateway;

    public BuildModeController(ProjectService projectService, OpenAiGateway openAiGateway) {
        this.projectService = projectService;
        this.openAiGateway = openAiGateway;
    }

    @GetMapping("/runtime")
    public AiRuntime runtime() {
        return new AiRuntime(openAiGateway.configured(), "OpenAI Responses API", "gpt-5.6-luna");
    }

    @PostMapping("/plan")
    public AgentResponse plan(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.PLANNING);
        String generated = generateOrFallback(PLAN_SYSTEM, request.prompt(), "Plan prepared in deterministic demo mode.");
        return new AgentResponse(
                "PLAN",
                project.id(),
                request.prompt(),
                List.of(
                        "Analyze requested product behavior and actors",
                        "Define screens, domain model and API contracts",
                        "Define implementation tasks and acceptance checks",
                        "Preserve project state until Build is requested"),
                generated,
                Instant.now());
    }

    @PostMapping("/build")
    public AgentResponse build(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.BUILDING);
        String generated = generateOrFallback(BUILD_SYSTEM, request.prompt(), "Build change-set prepared in deterministic demo mode.");
        projectService.updateStatus(project.id(), Project.ProjectStatus.READY);
        return new AgentResponse(
                "BUILD",
                project.id(),
                request.prompt(),
                List.of(
                        "Load current project context",
                        "Generate implementation change-set",
                        "Validate architecture and tests",
                        "Prepare workspace snapshot",
                        "Mark project ready for preview"),
                generated,
                Instant.now());
    }

    private String generateOrFallback(String system, String prompt, String fallback) {
        if (!openAiGateway.configured()) {
            return fallback + " Add OPENAI_API_KEY to enable live AI generation.";
        }
        try {
            return openAiGateway.generate(system, prompt);
        } catch (RuntimeException exception) {
            return fallback + " Live AI request failed safely: " + exception.getMessage();
        }
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

    public record AiRuntime(boolean configured, String provider, String model) {
    }
}
