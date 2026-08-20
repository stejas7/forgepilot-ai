package io.forgepilot.workspace;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory project file workspace with recoverable snapshots.
 *
 * @author Tejas Shah
 */
@Service
public class WorkspaceService {

    private final Map<UUID, LinkedHashMap<String, String>> filesByProject = new ConcurrentHashMap<>();
    private final Map<UUID, List<WorkspaceVersion>> versionsByProject = new ConcurrentHashMap<>();

    public synchronized List<WorkspaceFile> listFiles(UUID projectId) {
        return files(projectId).entrySet().stream()
                .map(entry -> new WorkspaceFile(entry.getKey(), entry.getValue()))
                .toList();
    }

    public synchronized WorkspaceFile getFile(UUID projectId, String path) {
        String content = files(projectId).get(path);
        if (content == null) {
            throw new WorkspaceFileNotFoundException(path);
        }
        return new WorkspaceFile(path, content);
    }

    public synchronized WorkspaceFile putFile(UUID projectId, String path, String content) {
        validatePath(path);
        files(projectId).put(path, content == null ? "" : content);
        return new WorkspaceFile(path, content == null ? "" : content);
    }

    public synchronized List<WorkspaceVersion> versions(UUID projectId) {
        return List.copyOf(versionsByProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()));
    }

    public synchronized WorkspaceVersion snapshot(UUID projectId, String label) {
        WorkspaceVersion version = new WorkspaceVersion(
                UUID.randomUUID(),
                label == null || label.isBlank() ? "Workspace snapshot" : label.trim(),
                Instant.now(),
                Map.copyOf(files(projectId)));
        versionsByProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()).add(0, version);
        return version;
    }

    public synchronized WorkspaceVersion restore(UUID projectId, UUID versionId) {
        WorkspaceVersion version = versions(projectId).stream()
                .filter(candidate -> candidate.id().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new WorkspaceVersionNotFoundException(versionId));
        filesByProject.put(projectId, new LinkedHashMap<>(version.files()));
        return version;
    }

    public synchronized void seedGeneratedApplication(UUID projectId, String prompt, String buildSummary) {
        LinkedHashMap<String, String> files = files(projectId);
        files.put("src/App.tsx", """
                export default function App() {
                  return (
                    <main>
                      <h1>Generated application</h1>
                      <p>%s</p>
                    </main>
                  )
                }
                """.formatted(escape(prompt)));
        files.put("src/styles.css", "body { font-family: Inter, system-ui, sans-serif; margin: 0; }\nmain { padding: 32px; }\n");
        files.put("server/ApplicationController.java", """
                package generated.app;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class ApplicationController {
                    @GetMapping("/api/status")
                    public String status() { return "READY"; }
                }
                """);
        files.put("db/schema.sql", "create table app_user (id uuid primary key, email varchar(200) unique not null, created_at timestamp not null);\n");
        files.put("BUILD.md", "# ForgePilot build\n\nPrompt:\n" + prompt + "\n\nAI summary:\n" + buildSummary + "\n");
    }

    private LinkedHashMap<String, String> files(UUID projectId) {
        return filesByProject.computeIfAbsent(projectId, ignored -> new LinkedHashMap<>());
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank() || path.startsWith("/") || path.contains("..")) {
            throw new IllegalArgumentException("Invalid workspace path");
        }
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    public record WorkspaceFile(String path, String content) {}

    public record WorkspaceVersion(UUID id, String label, Instant createdAt, Map<String, String> files) {}

    static class WorkspaceFileNotFoundException extends RuntimeException {
        WorkspaceFileNotFoundException(String path) { super("Workspace file not found: " + path); }
    }

    static class WorkspaceVersionNotFoundException extends RuntimeException {
        WorkspaceVersionNotFoundException(UUID id) { super("Workspace version not found: " + id); }
    }
}
