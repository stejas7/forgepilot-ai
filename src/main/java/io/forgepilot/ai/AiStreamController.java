package io.forgepilot.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/** Streams creator-facing command progress as newline-delimited JSON. */
@RestController
@RequestMapping("/api/ai")
public class AiStreamController {
    private final BuildModeController commands;
    private final ObjectMapper objectMapper;

    public AiStreamController(BuildModeController commands, ObjectMapper objectMapper) {
        this.commands = commands;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/{mode}/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public StreamingResponseBody stream(@PathVariable String mode,
                                        @RequestBody BuildModeController.AgentRequest request) {
        return output -> {
            emit(output, "status", "Loading project context");
            emit(output, "status", "Reading conversation and attachments");
            emit(output, "status", "Calling AI model");
            BuildModeController.AgentResponse result = "plan".equalsIgnoreCase(mode)
                    ? commands.plan(request)
                    : commands.build(request);
            for (String step : result.steps()) emit(output, "step", step);
            write(output, Map.of("type", "result", "result", result));
        };
    }

    private void emit(java.io.OutputStream output, String type, String message) throws Exception {
        write(output, Map.of("type", type, "message", message, "at", Instant.now().toString()));
    }

    private void write(java.io.OutputStream output, Object value) throws Exception {
        output.write((objectMapper.writeValueAsString(value) + "\n").getBytes(StandardCharsets.UTF_8));
        output.flush();
    }
}
