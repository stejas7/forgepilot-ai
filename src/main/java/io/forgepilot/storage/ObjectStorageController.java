package io.forgepilot.storage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/storage")
public class ObjectStorageController {
    private final ObjectStorageService service;
    public ObjectStorageController(ObjectStorageService service){ this.service = service; }

    @GetMapping("/objects") public List<ObjectStorageService.ObjectSummary> list(@PathVariable UUID projectId){ return service.list(projectId); }
    @GetMapping("/objects/{key}") public ObjectStorageService.StoredObject get(@PathVariable UUID projectId,@PathVariable String key){ return service.get(projectId,key); }
    @PostMapping("/objects") public ObjectStorageService.ObjectSummary put(@PathVariable UUID projectId,@Valid @RequestBody PutRequest request){ return service.put(projectId,request.key(),request.contentType(),request.base64()); }
    @DeleteMapping("/objects/{key}") public void delete(@PathVariable UUID projectId,@PathVariable String key){ service.delete(projectId,key); }
    @PostMapping("/scaffold") public ScaffoldResponse scaffold(@PathVariable UUID projectId){ return new ScaffoldResponse(service.scaffold(projectId)); }

    public record PutRequest(@NotBlank String key,String contentType,@NotBlank String base64){}
    public record ScaffoldResponse(String path){}
}
