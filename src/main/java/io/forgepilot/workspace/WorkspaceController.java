package io.forgepilot.workspace;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Project workspace and version history API.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/projects/{projectId}/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/files")
    public List<WorkspaceService.WorkspaceFile> files(@PathVariable UUID projectId) {
        return workspaceService.listFiles(projectId);
    }

    @GetMapping("/files/{*path}")
    public WorkspaceService.WorkspaceFile file(@PathVariable UUID projectId, @PathVariable String path) {
        return workspaceService.getFile(projectId, normalize(path));
    }

    @PutMapping("/files/{*path}")
    public WorkspaceService.WorkspaceFile update(
            @PathVariable UUID projectId,
            @PathVariable String path,
            @Valid @RequestBody FileUpdate request) {
        return workspaceService.putFile(projectId, normalize(path), request.content());
    }

    @GetMapping("/versions")
    public List<WorkspaceService.WorkspaceVersion> versions(@PathVariable UUID projectId) {
        return workspaceService.versions(projectId);
    }

    @PostMapping("/versions")
    public WorkspaceService.WorkspaceVersion snapshot(
            @PathVariable UUID projectId,
            @RequestBody(required = false) SnapshotRequest request) {
        return workspaceService.snapshot(projectId, request == null ? null : request.label());
    }

    @PostMapping("/versions/{versionId}/restore")
    public WorkspaceService.WorkspaceVersion restore(@PathVariable UUID projectId, @PathVariable UUID versionId) {
        return workspaceService.restore(projectId, versionId);
    }

    private String normalize(String path) {
        return path != null && path.startsWith("/") ? path.substring(1) : path;
    }

    public record FileUpdate(@NotBlank(message = "content is required") String content) {}
    public record SnapshotRequest(String label) {}
}
