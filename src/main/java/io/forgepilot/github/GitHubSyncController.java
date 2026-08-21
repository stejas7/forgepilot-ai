package io.forgepilot.github;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/github")
public class GitHubSyncController {
    private final GitHubSyncService service;
    public GitHubSyncController(GitHubSyncService service){this.service=service;}

    @GetMapping public Map<String,Object> status(@PathVariable UUID projectId){return Map.of("configured",service.configured(),"connection",service.get(projectId));}
    @PostMapping("/connect") public GitHubSyncService.Connection connect(@PathVariable UUID projectId,@Valid @RequestBody ConnectRequest request){return service.connect(projectId,request.owner(),request.repo(),request.branch());}
    @PostMapping("/push") public GitHubSyncService.SyncResult push(@PathVariable UUID projectId){return service.push(projectId);}
    @PostMapping("/pull") public GitHubSyncService.SyncResult pull(@PathVariable UUID projectId){return service.pull(projectId);}
    @DeleteMapping public void disconnect(@PathVariable UUID projectId){service.disconnect(projectId);}

    public record ConnectRequest(@NotBlank String owner,@NotBlank String repo,String branch){}
}
