package io.forgepilot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Server-side OpenAI Responses API gateway. The API key is read only from the
 * runtime environment and is never returned to clients.
 *
 * @author Tejas Shah
 */
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

    public boolean configured() {
        return StringUtils.hasText(apiKey);
    }

    public String generate(String systemInstruction, String userPrompt) {
        if (!configured()) {
            throw new IllegalStateException("OPENAI_API_KEY is not configured");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", List.of(Map.of("type", "input_text", "text", systemInstruction))),
                        Map.of(
                                "role", "user",
                                "content", List.of(Map.of("type", "input_text", "text", userPrompt))))) ;

        String raw = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(payload)
                .retrieve()
                .body(String.class);

        if (!StringUtils.hasText(raw)) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(raw);
            for (JsonNode output : root.path("output")) {
                for (JsonNode content : output.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        String text = content.path("text").asText();
                        if (StringUtils.hasText(text)) {
                            return text;
                        }
                    }
                }
            }
            throw new IllegalStateException("OpenAI response did not contain output_text");
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse OpenAI response", exception);
        }
    }
}
