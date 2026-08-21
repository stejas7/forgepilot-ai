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
    private static final String PLAN_SYSTEM = "You are ForgePilot AI's senior product architect. Convert the user's request into a concise, implementation-ready application plan. Cover actors, screens, domain entities, API contracts, security, data model, acceptance criteria, and implementation order. Do not claim files were changed.";
    private static final String BUILD_SYSTEM = "You are ForgePilot AI's senior full-stack engineer. Produce a concise implementation change-set for the requested application using React/TypeScript and Java 21/Spring Boot unless project context specifies otherwise.";

    private final ProjectService projectService;
    private final OpenAiGateway openAiGateway;
    private final WorkspaceService workspaceService;
    private final ConversationService conversationService;
    private final PlanService planService;
    private final UsageService usageService;
    private final AttachmentService attachmentService;

    public BuildModeController(ProjectService projectService, OpenAiGateway openAiGateway,
                               WorkspaceService workspaceService, ConversationService conversationService,
                               PlanService planService, UsageService usageService,
                               AttachmentService attachmentService) {
        this.projectService = projectService;
        this.openAiGateway = openAiGateway;
        this.workspaceService = workspaceService;
        this.conversationService = conversationService;
        this.planService = planService;
        this.usageService = usageService;
        this.attachmentService = attachmentService;
    }

    @GetMapping("/runtime")
    public AiRuntime runtime() { return new AiRuntime(openAiGateway.configured(), "OpenAI Responses API", "gpt-5.6-luna"); }

    @PostMapping("/plan")
    public AgentResponse plan(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.PLANNING);
        conversationService.addUser(project.id(), "PLAN", request.prompt());
        Generated generated = generate(project.id(), PLAN_SYSTEM, request.prompt(), "Plan prepared in deterministic demo mode.");
        planService.saveDraft(project.id(), generated.text());
        conversationService.addAssistant(project.id(), "PLAN", generated.text());
        usageService.record(project.id(), generated.inputTokens(), generated.outputTokens());
        return new AgentResponse("PLAN", project.id(), request.prompt(), List.of(
                "Analyze requested product behavior and actors",
                "Read attached text/image context",
                "Define screens, domain model and API contracts",
                "Save editable project plan without changing files"), generated.text(), Instant.now());
    }

    @PostMapping("/build")
    public AgentResponse build(@Valid @RequestBody AgentRequest request) {
        Project project = projectService.updateStatus(request.projectId(), Project.ProjectStatus.BUILDING);
        conversationService.addUser(project.id(), "BUILD", request.prompt());
        Generated generated = generate(project.id(), BUILD_SYSTEM, request.prompt(), "Build change-set prepared in deterministic demo mode.");
        workspaceService.seedGeneratedApplication(project.id(), request.prompt(), generated.text());
        workspaceService.snapshot(project.id(), "AI build: " + summarize(request.prompt()));
        projectService.updateStatus(project.id(), Project.ProjectStatus.READY);
        conversationService.addAssistant(project.id(), "BUILD", generated.text());
        usageService.record(project.id(), generated.inputTokens(), generated.outputTokens());
        return new AgentResponse("BUILD", project.id(), request.prompt(), List.of(
                "Load current project and attachment context",
                "Generate implementation change-set",
                "Write generated files to workspace",
                "Create recoverable version snapshot",
                "Mark project ready for preview"), generated.text(), Instant.now());
    }

    private Generated generate(UUID projectId, String system, String prompt, String fallback) {
        if (!openAiGateway.configured()) return new Generated(fallback + " Add OPENAI_API_KEY to enable live AI generation.", 0, 0);
        try {
            OpenAiGateway.GenerationResult result = openAiGateway.generateWithUsage(system, prompt, attachmentService.list(projectId));
            return new Generated(result.text(), result.inputTokens(), result.outputTokens());
        } catch (RuntimeException exception) {
            return new Generated(fallback + " Live AI request failed safely: " + exception.getMessage(), 0, 0);
        }
    }

    private String summarize(String prompt) {
        String value = prompt == null ? "Generated application" : prompt.trim();
        return value.length() <= 48 ? value : value.substring(0, 45) + "...";
    }

    private record Generated(String text, int inputTokens, int outputTokens) {}
    public record AgentRequest(UUID projectId, @NotBlank(message = "prompt is required") String prompt) {}
    public record AgentResponse(String mode, UUID projectId, String prompt, List<String> steps, String message, Instant createdAt) {}
    public record AiRuntime(boolean configured, String provider, String model) {}
}
