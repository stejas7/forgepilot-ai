package io.forgepilot.ai;

import io.forgepilot.project.Project;
import io.forgepilot.project.ProjectService;
import io.forgepilot.workspace.WorkspaceService;
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
    private final WorkspaceService workspaceService;
    private final ConversationService conversationService;
    private final PlanService planService;

    public BuildModeController(
            ProjectService projectService,
            OpenAiGateway openAiGateway,
            WorkspaceService workspaceService,
            ConversationService conversationService,
            PlanService planService) {
        this.projectService = projectService;
        this.openAiGateway = openAiGateway;
        this.workspaceService = workspaceService;
        this.conversationService = conversationService;
        this.planService = planService;
    }

    @GetMapping("/runtime")
    public AiRuntime runtime() {
        return new AiRuntime(openAiGateway.configured(), "OpenAI Responses API", "gpt-5.6-luna");
    }

    @PostMapping("/plan")
    public AgentResponse plan(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.PLANNING);
        conversationService.addUser(project.id(), "PLAN", request.prompt());
        String generated = generateOrFallback(PLAN_SYSTEM, request.prompt(), "Plan prepared in deterministic demo mode.");
        planService.saveDraft(project.id(), generated);
        conversationService.addAssistant(project.id(), "PLAN", generated);
        return new AgentResponse(
                "PLAN",
                project.id(),
                request.prompt(),
                List.of(
                        "Analyze requested product behavior and actors",
                        "Define screens, domain model and API contracts",
                        "Define implementation tasks and acceptance checks",
                        "Save editable project plan without changing files"),
                generated,
                Instant.now());
    }

    @PostMapping("/build")
    public AgentResponse build(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.BUILDING);
        conversationService.addUser(project.id(), "BUILD", request.prompt());
        String generated = generateOrFallback(BUILD_SYSTEM, request.prompt(), "Build change-set prepared in deterministic demo mode.");
        workspaceService.seedGeneratedApplication(project.id(), request.prompt(), generated);
        workspaceService.snapshot(project.id(), "AI build: " + summarize(request.prompt()));
        projectService.updateStatus(project.id(), Project.ProjectStatus.READY);
        conversationService.addAssistant(project.id(), "BUILD", generated);
        return new AgentResponse(
                "BUILD",
                project.id(),
                request.prompt(),
                List.of(
                        "Load current project context",
                        "Generate implementation change-set",
                        "Write generated files to workspace",
                        "Create recoverable version snapshot",
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

    private String summarize(String prompt) {
        String value = prompt == null ? "Generated application" : prompt.trim();
        return value.length() <= 48 ? value : value.substring(0, 45) + "...";
    }

    public record AgentRequest(UUID projectId, @NotBlank(message = "prompt is required") String prompt) {}
    public record AgentResponse(String mode, UUID projectId, String prompt, List<String> steps, String message, Instant createdAt) {}
    public record AiRuntime(boolean configured, String provider, String model) {}
}
