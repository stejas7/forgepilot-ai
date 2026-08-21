package io.forgepilot.workspace;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Search generated project files for the code workspace and AI context picker. */
@RestController
@RequestMapping("/api/projects/{projectId}/workspace/search")
public class WorkspaceSearchController {
    private final WorkspaceService workspaceService;

    public WorkspaceSearchController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public List<SearchResult> search(@PathVariable UUID projectId,
                                     @RequestParam(defaultValue = "") String q) {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return workspaceService.listFiles(projectId).stream()
                .filter(file -> needle.isBlank()
                        || file.path().toLowerCase(Locale.ROOT).contains(needle)
                        || file.content().toLowerCase(Locale.ROOT).contains(needle))
                .limit(100)
                .map(file -> new SearchResult(file.path(), excerpt(file.content(), needle)))
                .toList();
    }

    private String excerpt(String content, String needle) {
        if (content == null || content.isBlank()) return "";
        String oneLine = content.replace('\n', ' ').replace('\r', ' ');
        if (needle.isBlank()) return oneLine.substring(0, Math.min(180, oneLine.length()));
        int index = oneLine.toLowerCase(Locale.ROOT).indexOf(needle);
        int start = Math.max(0, index - 60);
        int end = Math.min(oneLine.length(), Math.max(index + needle.length() + 100, start + 180));
        return oneLine.substring(start, end);
    }

    public record SearchResult(String path, String excerpt) {}
}
