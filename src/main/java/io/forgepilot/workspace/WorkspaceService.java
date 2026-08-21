package io.forgepilot.workspace;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Durable project file workspace with recoverable snapshots.
 *
 * @author Tejas Shah
 */
@Service
public class WorkspaceService {

    private static final String FILES_STATE = "workspace-files.json";
    private static final String VERSIONS_STATE = "workspace-versions.json";

    private final PlatformStateStore stateStore;
    private final Map<UUID, LinkedHashMap<String, String>> filesByProject;
    private final Map<UUID, List<WorkspaceVersion>> versionsByProject;

    public WorkspaceService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        Map<UUID, LinkedHashMap<String, String>> loadedFiles = stateStore.read(
                FILES_STATE,
                new TypeReference<Map<UUID, LinkedHashMap<String, String>>>() {},
                LinkedHashMap::new);
        Map<UUID, List<WorkspaceVersion>> loadedVersions = stateStore.read(
                VERSIONS_STATE,
                new TypeReference<Map<UUID, List<WorkspaceVersion>>>() {},
                LinkedHashMap::new);
        this.filesByProject = new LinkedHashMap<>(loadedFiles);
        this.versionsByProject = new LinkedHashMap<>();
        loadedVersions.forEach((projectId, versions) ->
                this.versionsByProject.put(projectId, new ArrayList<>(versions)));
    }

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
        String value = content == null ? "" : content;
        files(projectId).put(path, value);
        persistFiles();
        return new WorkspaceFile(path, value);
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
        persistVersions();
        return version;
    }

    public synchronized WorkspaceVersion restore(UUID projectId, UUID versionId) {
        WorkspaceVersion version = versions(projectId).stream()
                .filter(candidate -> candidate.id().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new WorkspaceVersionNotFoundException(versionId));
        filesByProject.put(projectId, new LinkedHashMap<>(version.files()));
        persistFiles();
        return version;
    }

    public synchronized void seedGeneratedApplication(UUID projectId, String prompt, String buildSummary) {
        LinkedHashMap<String, String> files = files(projectId);
        String safePrompt = escape(prompt);
        String safeHtmlPrompt = escapeHtml(prompt);
        files.put("src/App.tsx", """
                export default function App() {
                  return (
                    <main>
                      <h1>Generated application</h1>
                      <p>%s</p>
                    </main>
                  )
                }
                """.formatted(safePrompt));
        files.put("src/styles.css", "body { font-family: Inter, system-ui, sans-serif; margin: 0; }\nmain { padding: 32px; }\n");
        files.put("preview/index.html", """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width,initial-scale=1" />
                  <title>ForgePilot Preview</title>
                  <style>
                    *{box-sizing:border-box}body{margin:0;font-family:Inter,system-ui,sans-serif;background:#f7f8fb;color:#17191f}
                    .app{min-height:100vh;display:grid;grid-template-columns:210px 1fr}.nav{background:#12151d;color:#fff;padding:28px 20px}.nav h2{margin:0 0 28px}.nav span{display:block;padding:9px 10px;border-radius:8px;margin:4px 0;color:#cbd1dc}.nav span.active{background:#242a35;color:#fff}.main{padding:42px}.eyebrow{font-size:11px;letter-spacing:.14em;color:#8b92a0}.main h1{font-size:38px;margin:8px 0 10px}.main p{max-width:760px;color:#697180;line-height:1.6}.stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px;margin:30px 0}.card{background:#fff;border:1px solid #e5e8ee;border-radius:15px;padding:20px;box-shadow:0 8px 25px rgba(24,30,42,.05)}.card b{font-size:25px;display:block}.card span{font-size:12px;color:#7a8290}.panel{background:#fff;border:1px solid #e5e8ee;border-radius:15px;padding:22px}.cta{display:inline-block;margin-top:18px;border:0;background:#17191f;color:white;padding:11px 16px;border-radius:10px;font-weight:700}
                    @media(max-width:720px){.app{grid-template-columns:1fr}.nav{display:none}.main{padding:24px}.stats{grid-template-columns:1fr}}
                  </style>
                </head>
                <body>
                  <div class="app">
                    <aside class="nav"><h2>ForgePilot App</h2><span class="active">Dashboard</span><span>Customers</span><span>Workflows</span><span>Settings</span></aside>
                    <main class="main"><div class="eyebrow">GENERATED APPLICATION</div><h1>Your app is taking shape</h1><p>%s</p><div class="stats"><div class="card"><b>1,284</b><span>Customers</span></div><div class="card"><b>87%%</b><span>Completion</span></div><div class="card"><b>24</b><span>Active workflows</span></div></div><section class="panel"><h3>Ready for the next prompt</h3><p>This preview is generated from the project workspace and refreshes after Build requests.</p><button class="cta">Primary action</button></section></main>
                  </div>
                </body>
                </html>
                """.formatted(safeHtmlPrompt));
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
        persistFiles();
    }

    private LinkedHashMap<String, String> files(UUID projectId) {
        return filesByProject.computeIfAbsent(projectId, ignored -> new LinkedHashMap<>());
    }

    private void persistFiles() {
        stateStore.write(FILES_STATE, filesByProject);
    }

    private void persistVersions() {
        stateStore.write(VERSIONS_STATE, versionsByProject);
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank() || path.startsWith("/") || path.contains("..")) {
            throw new IllegalArgumentException("Invalid workspace path");
        }
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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
