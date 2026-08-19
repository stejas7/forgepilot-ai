package io.forgepilot.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Project workspace REST API.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> list() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public Project get(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.create(request.name(), request.description(), request.stack());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PatchMapping("/{id}/status")
    public Project updateStatus(@PathVariable UUID id, @RequestBody UpdateStatusRequest request) {
        return projectService.updateStatus(id, request.status());
    }

    @ExceptionHandler(ProjectService.ProjectNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(ProjectService.ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    public record CreateProjectRequest(
            @NotBlank(message = "name is required") String name,
            String description,
            String stack) {
    }

    public record UpdateStatusRequest(Project.ProjectStatus status) {
    }
}
