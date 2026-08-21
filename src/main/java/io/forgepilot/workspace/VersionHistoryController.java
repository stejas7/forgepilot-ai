package io.forgepilot.workspace;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Serves historical version previews. Diff/restore endpoints are owned by
 * {@link WorkspaceController} so Spring MVC has a single mapping per route.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/workspace/versions")
public class VersionHistoryController {

    private final WorkspaceService workspaceService;

    public VersionHistoryController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping(value = "/{versionId}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable UUID projectId, @PathVariable UUID versionId) {
        WorkspaceService.WorkspaceVersion version = findVersion(projectId, versionId);
        String html = version.files().get("preview/index.html");
        if (html == null) {
            html = "<!doctype html><html><body style='font-family:system-ui;padding:40px'><h2>No preview in this version</h2></body></html>";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header("Cache-Control", "no-store")
                .body(html);
    }

    private WorkspaceService.WorkspaceVersion findVersion(UUID projectId, UUID versionId) {
        return workspaceService.versions(projectId).stream()
                .filter(version -> version.id().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId));
    }
}
