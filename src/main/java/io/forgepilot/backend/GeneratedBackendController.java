package io.forgepilot.backend;

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

@RestController
@RequestMapping("/api/projects/{projectId}/backend")
public class GeneratedBackendController {
    private final GeneratedBackendService service;
    public GeneratedBackendController(GeneratedBackendService service) { this.service = service; }

    @GetMapping public GeneratedBackendService.BackendProject get(@PathVariable UUID projectId){ return service.get(projectId); }
    @PostMapping("/provision") public GeneratedBackendService.BackendProject provision(@PathVariable UUID projectId){ return service.provision(projectId); }
    @PutMapping("/auth") public GeneratedBackendService.BackendProject auth(@PathVariable UUID projectId,@RequestBody AuthRequest request){ return service.configureAuth(projectId,request.emailPassword(),request.oauth(),request.rbac()); }
    @PutMapping("/secrets") public GeneratedBackendService.BackendProject secret(@PathVariable UUID projectId,@Valid @RequestBody SecretRequest request){ return service.putSecret(projectId,request.name(),request.value()); }
    @PostMapping("/tables") public GeneratedBackendService.BackendProject table(@PathVariable UUID projectId,@Valid @RequestBody TableRequest request){ return service.registerTable(projectId,request.name(),request.columns()); }
    @GetMapping("/logs") public List<String> logs(@PathVariable UUID projectId){ return service.logs(projectId); }

    public record AuthRequest(boolean emailPassword,boolean oauth,boolean rbac){}
    public record SecretRequest(@NotBlank String name,@NotBlank String value){}
    public record TableRequest(@NotBlank String name,List<GeneratedBackendService.Column> columns){}
}
