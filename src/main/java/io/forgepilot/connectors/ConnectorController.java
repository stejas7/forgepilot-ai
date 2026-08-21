package io.forgepilot.connectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ConnectorController {
    private final ConnectorService service;
    public ConnectorController(ConnectorService service){this.service=service;}

    @GetMapping("/connectors/catalog") public List<ConnectorService.CatalogItem> catalog(){return service.catalog();}
    @GetMapping("/projects/{projectId}/connectors") public List<ConnectorService.Connection> list(@PathVariable UUID projectId){return service.list(projectId);}
    @PostMapping("/projects/{projectId}/connectors/{connectorId}") public ConnectorService.Connection connect(@PathVariable UUID projectId,@PathVariable String connectorId,@RequestBody(required=false) Map<String,String> config){return service.connect(projectId,connectorId,config==null?Map.of():config);}
    @PostMapping("/projects/{projectId}/connectors/{connectorId}/health") public ConnectorService.Connection health(@PathVariable UUID projectId,@PathVariable String connectorId){return service.health(projectId,connectorId);}
    @DeleteMapping("/projects/{projectId}/connectors/{connectorId}") public void disconnect(@PathVariable UUID projectId,@PathVariable String connectorId){service.disconnect(projectId,connectorId);}
}
