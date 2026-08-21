package io.forgepilot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiGateway(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${forgepilot.ai.openai.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${forgepilot.ai.openai.model:gpt-5.6-luna}") String model) {
        this.restClient = restClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean configured() { return StringUtils.hasText(apiKey); }

    public String generate(String systemInstruction, String userPrompt) {
        return generateWithUsage(systemInstruction, userPrompt, List.of()).text();
    }

    public GenerationResult generateWithUsage(String systemInstruction, String userPrompt) {
        return generateWithUsage(systemInstruction, userPrompt, List.of());
    }

    public GenerationResult generateWithUsage(String systemInstruction, String userPrompt,
                                               List<AttachmentService.Attachment> attachments) {
        if (!configured()) throw new IllegalStateException("OPENAI_API_KEY is not configured");

        List<Map<String, Object>> userContent = new ArrayList<>();
        userContent.add(Map.of("type", "input_text", "text", userPrompt));
        for (AttachmentService.Attachment attachment : attachments.stream().limit(6).toList()) {
            if (attachment.mimeType().startsWith("image/")) {
                userContent.add(Map.of(
                        "type", "input_image",
                        "image_url", "data:" + attachment.mimeType() + ";base64," + attachment.base64()));
            } else if (attachment.mimeType().startsWith("text/") || attachment.name().matches(".*\\.(md|txt|json|csv|xml|yaml|yml)$")) {
                String text = new String(Base64.getDecoder().decode(attachment.base64()), StandardCharsets.UTF_8);
                userContent.add(Map.of("type", "input_text", "text", "Attachment " + attachment.name() + ":\n" + text));
            }
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "input", List.of(
                        Map.of("role", "system", "content", List.of(Map.of("type", "input_text", "text", systemInstruction))),
                        Map.of("role", "user", "content", userContent)));

        String raw = restClient.post().uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(payload).retrieve().body(String.class);
        if (!StringUtils.hasText(raw)) throw new IllegalStateException("OpenAI returned an empty response");

        try {
            JsonNode root = objectMapper.readTree(raw);
            String text = null;
            for (JsonNode output : root.path("output")) {
                for (JsonNode content : output.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        String candidate = content.path("text").asText();
                        if (StringUtils.hasText(candidate)) { text = candidate; break; }
                    }
                }
            }
            if (!StringUtils.hasText(text)) throw new IllegalStateException("OpenAI response did not contain output_text");
            return new GenerationResult(text,
                    root.path("usage").path("input_tokens").asInt(0),
                    root.path("usage").path("output_tokens").asInt(0));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse OpenAI response", exception);
        }
    }

    public record GenerationResult(String text, int inputTokens, int outputTokens) {}
}
