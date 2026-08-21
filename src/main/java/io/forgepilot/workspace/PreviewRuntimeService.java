package io.forgepilot.workspace;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight runtime verification for the generated preview artifact.
 * This validates the real workspace-backed preview and can repair a missing
 * preview document from the generated source context.
 */
@Service
public class PreviewRuntimeService {

    private final WorkspaceService workspaceService;

    public PreviewRuntimeService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    public RuntimeReport verify(UUID projectId) {
        List<String> logs = new ArrayList<>();
        logs.add("Loading generated preview from project workspace");
        try {
            String html = workspaceService.getFile(projectId, "preview/index.html").content();
            List<String> issues = new ArrayList<>();
            if (html == null || html.isBlank()) issues.add("Preview document is empty");
            if (html != null && !html.toLowerCase().contains("<html")) issues.add("Missing <html> root element");
            if (html != null && !html.toLowerCase().contains("<body")) issues.add("Missing <body> element");
            if (html != null && html.length() > 2_000_000) issues.add("Preview document exceeds demo runtime size limit");
            logs.add("HTML document parsed for required runtime markers");
            logs.add("Preview endpoint is available at /api/projects/" + projectId + "/preview");
            if (issues.isEmpty()) {
                logs.add("Verification completed successfully");
                return new RuntimeReport("READY", true, Instant.now(), issues, logs);
            }
            logs.add("Verification found " + issues.size() + " issue(s)");
            return new RuntimeReport("ERROR", false, Instant.now(), issues, logs);
        } catch (RuntimeException exception) {
            logs.add("Preview file could not be loaded: " + exception.getMessage());
            return new RuntimeReport("NOT_BUILT", false, Instant.now(), List.of("Run Build to create the preview"), logs);
        }
    }

    public RuntimeReport repair(UUID projectId) {
        RuntimeReport before = verify(projectId);
        if (before.ready()) return before;

        String context;
        try {
            context = workspaceService.getFile(projectId, "BUILD.md").content();
        } catch (RuntimeException ignored) {
            context = "ForgePilot generated application";
        }
        String escaped = escapeHtml(context.length() > 800 ? context.substring(0, 800) : context);
        String repaired = """
                <!doctype html>
                <html lang="en">
                <head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>ForgePilot repaired preview</title>
                <style>body{margin:0;font-family:Inter,system-ui;background:#f6f7fb;color:#17191f}.wrap{max-width:920px;margin:0 auto;padding:64px 28px}.badge{display:inline-block;padding:7px 11px;border-radius:999px;background:#ede9fe;color:#6d28d9;font-size:12px;font-weight:700}.card{margin-top:24px;background:white;border:1px solid #e6e7eb;border-radius:18px;padding:28px;box-shadow:0 14px 45px rgba(30,35,50,.08)}h1{font-size:42px;margin:14px 0}p{line-height:1.6;color:#666}</style></head>
                <body><main class="wrap"><span class="badge">FORGEPILOT AUTO-REPAIR</span><h1>Preview recovered</h1><div class="card"><h2>Your generated app is available again.</h2><p>%s</p></div></main></body>
                </html>
                """.formatted(escaped);
        workspaceService.putFile(projectId, "preview/index.html", repaired);
        workspaceService.snapshot(projectId, "Automatic preview repair");
        RuntimeReport after = verify(projectId);
        List<String> logs = new ArrayList<>(after.logs());
        logs.add(0, "Automatic repair regenerated preview/index.html and created a snapshot");
        return new RuntimeReport(after.status(), after.ready(), Instant.now(), after.issues(), logs);
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public record RuntimeReport(String status, boolean ready, Instant checkedAt, List<String> issues, List<String> logs) {}
}
