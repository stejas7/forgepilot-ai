package io.forgepilot.readiness;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readiness")
public class ProductionReadinessController {
    private final ProductionReadinessService service;
    public ProductionReadinessController(ProductionReadinessService service){this.service=service;}
    @GetMapping public ProductionReadinessService.ReadinessReport readiness(){return service.evaluate();}
}
