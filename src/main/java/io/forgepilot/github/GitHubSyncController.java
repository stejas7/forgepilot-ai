package io.forgepilot.github;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/github")
public class GitHubSyncController {
    private final GitHubSyncService service;
    public GitHubSyncController(GitHubSyncService service){this.service=service;}

    @GetMapping public Map<String,Object> status(@PathVariable UUID projectId){
        Map<String,Object> result=new LinkedHashMap<>();result.put("configured",service.configured());result.put("connection",service.get(projectId));
        if(service.configured()&&service.get(projectId)!=null)result.put("sync",service.inspect(projectId));
        return result;
    }
    @PostMapping("/create") public GitHubSyncService.Connection create(@PathVariable UUID projectId,@Valid @RequestBody CreateRequest request){return service.createRepository(projectId,request.name(),request.privateRepo());}
    @PostMapping("/connect") public GitHubSyncService.Connection connect(@PathVariable UUID projectId,@Valid @RequestBody ConnectRequest request){return service.connect(projectId,request.owner(),request.repo(),request.branch());}
    @PostMapping("/push") public GitHubSyncService.SyncResult push(@PathVariable UUID projectId,@RequestParam(defaultValue="false") boolean force){return service.push(projectId,force);}
    @PostMapping("/pull") public GitHubSyncService.SyncResult pull(@PathVariable UUID projectId,@RequestParam(defaultValue="false") boolean force){return service.pull(projectId,force);}
    @DeleteMapping public void disconnect(@PathVariable UUID projectId){service.disconnect(projectId);}

    public record CreateRequest(@NotBlank String name,boolean privateRepo){}
    public record ConnectRequest(@NotBlank String owner,@NotBlank String repo,String branch){}
}
