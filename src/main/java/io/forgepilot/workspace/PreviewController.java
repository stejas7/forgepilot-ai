package io.forgepilot.workspace;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Serves the generated application preview document for the builder iframe.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/projects/{projectId}/preview")
public class PreviewController {

    private final WorkspaceService workspaceService;

    public PreviewController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable UUID projectId) {
        try {
            String html = workspaceService.getFile(projectId, "preview/index.html").content();
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header("Cache-Control", "no-store")
                    .body(html);
        } catch (RuntimeException exception) {
            String empty = """
                    <!doctype html><html><body style="font-family:system-ui;padding:40px;color:#616875">
                    <h2>Ready to build</h2><p>Run Build to generate the first application preview.</p>
                    </body></html>
                    """;
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(empty);
        }
    }
}
