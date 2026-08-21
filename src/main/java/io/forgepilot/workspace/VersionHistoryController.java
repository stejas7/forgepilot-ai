package io.forgepilot.workspace;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/workspace/versions")
public class VersionHistoryController {

    private final WorkspaceService workspaceService;

    public VersionHistoryController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/{versionId}/diff")
    public VersionDiff diff(@PathVariable UUID projectId, @PathVariable UUID versionId) {
        WorkspaceService.WorkspaceVersion version = findVersion(projectId, versionId);
        Map<String, String> current = new LinkedHashMap<>();
        workspaceService.listFiles(projectId).forEach(file -> current.put(file.path(), file.content()));
        List<FileDiff> files = new ArrayList<>();
        java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();
        paths.addAll(version.files().keySet());
        paths.addAll(current.keySet());
        for (String path : paths) {
            String before = version.files().get(path);
            String after = current.get(path);
            if (java.util.Objects.equals(before, after)) continue;
            String change = before == null ? "ADDED" : after == null ? "DELETED" : "MODIFIED";
            files.add(new FileDiff(path, change, changedLines(before, after)));
        }
        return new VersionDiff(version.id(), version.label(), files.size(), files);
    }

    @GetMapping(value = "/{versionId}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable UUID projectId, @PathVariable UUID versionId) {
        WorkspaceService.WorkspaceVersion version = findVersion(projectId, versionId);
        String html = version.files().get("preview/index.html");
        if (html == null) {
            html = "<!doctype html><html><body style='font-family:system-ui;padding:40px'><h2>No preview in this version</h2></body></html>";
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).header("Cache-Control", "no-store").body(html);
    }

    private WorkspaceService.WorkspaceVersion findVersion(UUID projectId, UUID versionId) {
        return workspaceService.versions(projectId).stream()
                .filter(version -> version.id().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId));
    }

    private int changedLines(String before, String after) {
        String[] left = before == null ? new String[0] : before.split("\\R", -1);
        String[] right = after == null ? new String[0] : after.split("\\R", -1);
        int changed = Math.abs(left.length - right.length);
        for (int i = 0; i < Math.min(left.length, right.length); i++) {
            if (!left[i].equals(right[i])) changed++;
        }
        return changed;
    }

    public record VersionDiff(UUID versionId, String label, int changedFiles, List<FileDiff> files) {}
    public record FileDiff(String path, String change, int changedLines) {}
}
