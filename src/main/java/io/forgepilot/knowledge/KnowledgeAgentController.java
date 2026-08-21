package io.forgepilot.knowledge;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeAgentController {
    private final KnowledgeAgentService service;
    public KnowledgeAgentController(KnowledgeAgentService service){this.service=service;}
    @GetMapping public KnowledgeAgentService.State state(){return service.state();}
    @PostMapping("/items") public KnowledgeAgentService.KnowledgeItem add(@RequestBody KnowledgeRequest r){return service.addKnowledge(r.scope(),r.title(),r.content(),r.tags());}
    @GetMapping("/retrieve") public Object retrieve(@RequestParam String q,@RequestParam(defaultValue="6") int limit){return service.retrieve(q,limit);}
    @GetMapping("/context") public KnowledgeAgentService.ContextBundle context(@RequestParam String q){return service.context(q);}
    @PostMapping("/templates") public KnowledgeAgentService.Template template(@RequestBody TemplateRequest r){return service.addTemplate(r.name(),r.description(),r.prompt(),r.stack());}
    @PostMapping("/skills") public KnowledgeAgentService.Skill skill(@RequestBody SkillRequest r){return service.addSkill(r.name(),r.instruction(),r.enabled());}
    @PostMapping("/queue") public KnowledgeAgentService.AgentTask enqueue(@RequestBody QueueRequest r){return service.enqueue(r.projectId(),r.prompt(),r.agent());}
    @PostMapping("/queue/{id}/status") public KnowledgeAgentService.AgentTask transition(@PathVariable UUID id,@RequestBody QueueStatus r){return service.transition(id,r.status(),r.result());}
    public record KnowledgeRequest(String scope,String title,String content,List<String> tags){}
    public record TemplateRequest(String name,String description,String prompt,String stack){}
    public record SkillRequest(String name,String instruction,boolean enabled){}
    public record QueueRequest(UUID projectId,String prompt,String agent){}
    public record QueueStatus(String status,String result){}
}
