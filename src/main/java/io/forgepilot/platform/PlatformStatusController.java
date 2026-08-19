package io.forgepilot.platform;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformStatusController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "product", "ForgePilot AI",
                "status", "UP",
                "version", "0.1.0",
                "timestamp", Instant.now().toString(),
                "capabilities", List.of(
                        "plan",
                        "build",
                        "code-workspace",
                        "live-preview",
                        "versioning",
                        "github-sync",
                        "deployment"));
    }
}
