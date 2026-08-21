package io.forgepilot.workspace;

import com.fasterxml.jackson.core.type.TypeReference;
import io.forgepilot.platform.PlatformStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Durable project file workspace with recoverable snapshots. */
@Service
public class WorkspaceService {

    private static final String FILES_STATE = "workspace-files.json";
    private static final String VERSIONS_STATE = "workspace-versions.json";

    private final PlatformStateStore stateStore;
    private final Map<UUID, LinkedHashMap<String, String>> filesByProject;
    private final Map<UUID, List<WorkspaceVersion>> versionsByProject;

    public WorkspaceService(PlatformStateStore stateStore) {
        this.stateStore = stateStore;
        Map<UUID, LinkedHashMap<String, String>> loadedFiles = stateStore.read(FILES_STATE,
                new TypeReference<Map<UUID, LinkedHashMap<String, String>>>() {}, LinkedHashMap::new);
        Map<UUID, List<WorkspaceVersion>> loadedVersions = stateStore.read(VERSIONS_STATE,
                new TypeReference<Map<UUID, List<WorkspaceVersion>>>() {}, LinkedHashMap::new);
        this.filesByProject = new LinkedHashMap<>(loadedFiles);
        this.versionsByProject = new LinkedHashMap<>();
        loadedVersions.forEach((projectId, versions) -> this.versionsByProject.put(projectId, new ArrayList<>(versions)));
    }

    public synchronized List<WorkspaceFile> listFiles(UUID projectId) {
        return files(projectId).entrySet().stream().map(entry -> new WorkspaceFile(entry.getKey(), entry.getValue())).toList();
    }

    public synchronized WorkspaceFile getFile(UUID projectId, String path) {
        String content = files(projectId).get(path);
        if (content == null) throw new WorkspaceFileNotFoundException(path);
        return new WorkspaceFile(path, content);
    }

    public synchronized WorkspaceFile putFile(UUID projectId, String path, String content) {
        validatePath(path);
        String value = content == null ? "" : content;
        files(projectId).put(path, value);
        persistFiles();
        return new WorkspaceFile(path, value);
    }

    public synchronized WorkspaceFile createFile(UUID projectId, String path, String content) {
        validatePath(path);
        if (files(projectId).containsKey(path)) throw new IllegalArgumentException("Workspace file already exists: " + path);
        return putFile(projectId, path, content);
    }

    public synchronized WorkspaceFile renameFile(UUID projectId, String from, String to) {
        validatePath(from); validatePath(to);
        LinkedHashMap<String, String> projectFiles = files(projectId);
        String content = projectFiles.get(from);
        if (content == null) throw new WorkspaceFileNotFoundException(from);
        if (!from.equals(to) && projectFiles.containsKey(to)) throw new IllegalArgumentException("Workspace file already exists: " + to);
        LinkedHashMap<String, String> reordered = new LinkedHashMap<>();
        projectFiles.forEach((path, value) -> reordered.put(path.equals(from) ? to : path, value));
        filesByProject.put(projectId, reordered);
        persistFiles();
        return new WorkspaceFile(to, content);
    }

    public synchronized void deleteFile(UUID projectId, String path) {
        validatePath(path);
        if (files(projectId).remove(path) == null) throw new WorkspaceFileNotFoundException(path);
        persistFiles();
    }

    public synchronized List<WorkspaceVersion> versions(UUID projectId) {
        return List.copyOf(versionsByProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()));
    }

    public synchronized WorkspaceVersion snapshot(UUID projectId, String label) {
        WorkspaceVersion version = new WorkspaceVersion(UUID.randomUUID(),
                label == null || label.isBlank() ? "Workspace snapshot" : label.trim(), Instant.now(), Map.copyOf(files(projectId)));
        versionsByProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()).add(0, version);
        persistVersions();
        return version;
    }

    public synchronized WorkspaceVersion restore(UUID projectId, UUID versionId) {
        WorkspaceVersion version = version(projectId, versionId);
        filesByProject.put(projectId, new LinkedHashMap<>(version.files()));
        persistFiles();
        return version;
    }

    public synchronized VersionDiff diff(UUID projectId, UUID versionId) {
        WorkspaceVersion version = version(projectId, versionId);
        Map<String, String> current = files(projectId);
        Set<String> paths = new LinkedHashSet<>();
        paths.addAll(version.files().keySet()); paths.addAll(current.keySet());
        List<FileDiff> diffs = new ArrayList<>();
        for (String path : paths) {
            String before = version.files().get(path);
            String after = current.get(path);
            String status = before == null ? "ADDED" : after == null ? "DELETED" : before.equals(after) ? "UNCHANGED" : "MODIFIED";
            if (!"UNCHANGED".equals(status)) diffs.add(new FileDiff(path, status, before, after));
        }
        return new VersionDiff(version.id(), version.label(), version.createdAt(), diffs);
    }

    public synchronized void seedGeneratedApplication(UUID projectId, String prompt, String buildSummary) {
        LinkedHashMap<String, String> files = files(projectId);
        String safePrompt = escape(prompt), safeHtmlPrompt = escapeHtml(prompt);
        files.put("src/App.tsx", "export default function App(){return <main><h1>Generated application</h1><p>" + safePrompt + "</p></main>}\n");
        files.put("src/styles.css", "body { font-family: Inter, system-ui, sans-serif; margin: 0; }\nmain { padding: 32px; }\n");
        files.put("preview/index.html", """
                <!doctype html><html lang="en"><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/>
                <title>ForgePilot Preview</title><style>*{box-sizing:border-box}body{margin:0;font-family:Inter,system-ui,sans-serif;background:#f7f8fb;color:#17191f}.app{min-height:100vh;display:grid;grid-template-columns:210px 1fr}.nav{background:#12151d;color:#fff;padding:28px 20px}.nav span{display:block;padding:9px 10px;margin:4px 0}.main{padding:42px}.card{background:#fff;border:1px solid #e5e8ee;border-radius:15px;padding:20px}@media(max-width:720px){.app{grid-template-columns:1fr}.nav{display:none}.main{padding:24px}}</style></head>
                <body><div class="app"><aside class="nav"><h2>ForgePilot App</h2><span>Dashboard</span><span>Customers</span><span>Settings</span></aside><main class="main"><h1>Your app is taking shape</h1><p>%s</p><div class="card"><h3>Ready for the next prompt</h3></div></main></div></body></html>
                """.formatted(safeHtmlPrompt));
        files.put("server/ApplicationController.java", "package generated.app; public class ApplicationController {}\n");
        files.put("db/schema.sql", "create table app_user (id uuid primary key, email varchar(200) unique not null, created_at timestamp not null);\n");
        files.put("BUILD.md", "# ForgePilot build\n\nPrompt:\n" + prompt + "\n\nAI summary:\n" + buildSummary + "\n");
        persistFiles();
    }

    private WorkspaceVersion version(UUID projectId, UUID versionId) {
        return versions(projectId).stream().filter(candidate -> candidate.id().equals(versionId)).findFirst()
                .orElseThrow(() -> new WorkspaceVersionNotFoundException(versionId));
    }
    private LinkedHashMap<String, String> files(UUID projectId) { return filesByProject.computeIfAbsent(projectId, ignored -> new LinkedHashMap<>()); }
    private void persistFiles() { stateStore.write(FILES_STATE, filesByProject); }
    private void persistVersions() { stateStore.write(VERSIONS_STATE, versionsByProject); }
    private void validatePath(String path) { if (path == null || path.isBlank() || path.startsWith("/") || path.contains("..")) throw new IllegalArgumentException("Invalid workspace path"); }
    private String escape(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "); }
    private String escapeHtml(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }

    public record WorkspaceFile(String path, String content) {}
    public record WorkspaceVersion(UUID id, String label, Instant createdAt, Map<String, String> files) {}
    public record FileDiff(String path, String status, String before, String after) {}
    public record VersionDiff(UUID versionId, String label, Instant createdAt, List<FileDiff> files) {}
    static class WorkspaceFileNotFoundException extends RuntimeException { WorkspaceFileNotFoundException(String path) { super("Workspace file not found: " + path); } }
    static class WorkspaceVersionNotFoundException extends RuntimeException { WorkspaceVersionNotFoundException(UUID id) { super("Workspace version not found: " + id); } }
}
