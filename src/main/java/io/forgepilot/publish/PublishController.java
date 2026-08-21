package io.forgepilot.publish;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
public class PublishController {
    private final PublishService service;
    public PublishController(PublishService service){this.service=service;}

    @GetMapping("/api/projects/{projectId}/publishes") public List<PublishService.Release> history(@PathVariable UUID projectId){return service.history(projectId);}
    @PostMapping("/api/projects/{projectId}/publishes") public PublishService.Release request(@PathVariable UUID projectId,@RequestBody PublishRequest r){return service.request(projectId,r.actor(),r.visibility());}
    @PostMapping("/api/projects/{projectId}/publishes/{releaseId}/approve") public PublishService.Release approve(@PathVariable UUID projectId,@PathVariable UUID releaseId,@RequestBody Actor r){return service.approve(projectId,releaseId,r.actor());}
    @PostMapping("/api/projects/{projectId}/publishes/{releaseId}/reject") public PublishService.Release reject(@PathVariable UUID projectId,@PathVariable UUID releaseId,@RequestBody Actor r){return service.reject(projectId,releaseId,r.actor());}
    @PostMapping("/api/projects/{projectId}/publishes/{releaseId}/publish") public PublishService.Release publish(@PathVariable UUID projectId,@PathVariable UUID releaseId,@RequestBody Actor r){return service.publish(projectId,releaseId,r.actor());}
    @PostMapping("/api/projects/{projectId}/publishes/{releaseId}/rollback") public PublishService.Release rollback(@PathVariable UUID projectId,@PathVariable UUID releaseId,@RequestBody Actor r){return service.rollback(projectId,releaseId,r.actor());}
    @DeleteMapping("/api/projects/{projectId}/publishes") public void unpublish(@PathVariable UUID projectId,@RequestParam(defaultValue="system") String actor){service.unpublish(projectId,actor);}
    @GetMapping(value="/published/{projectId}",produces=MediaType.TEXT_HTML_VALUE) public String published(@PathVariable UUID projectId){return service.publishedHtml(projectId);}

    public record PublishRequest(String actor,String visibility){}
    public record Actor(String actor){}
}
