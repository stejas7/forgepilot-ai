package io.forgepilot.collaboration;

import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceCollaborationController {
    private final WorkspaceCollaborationService service;
    public WorkspaceCollaborationController(WorkspaceCollaborationService service){this.service=service;}
    @GetMapping("/collaboration") public WorkspaceCollaborationService.State state(){return service.state();}
    @PostMapping("/members") public WorkspaceCollaborationService.Member invite(@RequestBody Invite r){return service.invite(r.email(),r.role(),r.actor());}
    @PutMapping("/members/{id}/role") public WorkspaceCollaborationService.Member role(@PathVariable UUID id,@RequestBody RoleChange r){return service.setRole(id,r.role(),r.actor());}
    @PostMapping("/projects/{projectId}/share") public void share(@PathVariable UUID projectId,@RequestBody Share r){service.share(projectId,r.email(),r.role(),r.actor());}
    @PostMapping("/projects/{projectId}/comments") public WorkspaceCollaborationService.Comment comment(@PathVariable UUID projectId,@RequestBody CommentRequest r){return service.comment(projectId,r.actor(),r.body(),r.target());}
    @GetMapping("/activity") public Object activity(){return service.activity();}
    @GetMapping("/permissions/{role}/{action}") public boolean allowed(@PathVariable WorkspaceCollaborationService.Role role,@PathVariable String action){return service.allowed(role,action.toUpperCase());}
    public record Invite(String email,WorkspaceCollaborationService.Role role,String actor){}
    public record RoleChange(WorkspaceCollaborationService.Role role,String actor){}
    public record Share(String email,WorkspaceCollaborationService.Role role,String actor){}
    public record CommentRequest(String actor,String body,String target){}
}
