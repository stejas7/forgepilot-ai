package io.forgepilot.workspace;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/runtime")
public class PreviewRuntimeController {

    private final PreviewRuntimeService runtimeService;

    public PreviewRuntimeController(PreviewRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @GetMapping
    public PreviewRuntimeService.RuntimeReport verify(@PathVariable UUID projectId) {
        return runtimeService.verify(projectId);
    }

    @PostMapping("/repair")
    public PreviewRuntimeService.RuntimeReport repair(@PathVariable UUID projectId) {
        return runtimeService.repair(projectId);
    }
}
