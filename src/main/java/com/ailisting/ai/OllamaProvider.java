package com.ailisting.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Ollama AI Provider implementation.
 *
 * WHY WebFlux WebClient?
 * - Non-blocking HTTP calls to Ollama REST API
 * - Better than RestTemplate for external service calls
 * - Timeout support built-in
 *
 * OLLAMA API FORMAT:
 * POST /api/generate  → Raw text generation
 * POST /api/chat      → Chat-style generation
 * We use /api/generate for simplicity with small models.
 */
@Slf4j
public class OllamaProvider implements AiProvider {

    private final WebClient webClient;
    private final String model;
    private final long timeoutMs;

    public OllamaProvider(WebClient webClient, String model, long timeoutMs) {
        this.webClient = webClient;
        this.model = model;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public String generate(String prompt, Map<String, Object> parameters) {
        log.debug("Ollama generate: model={}, promptLength={}", model, prompt.length());

        ObjectNode requestBody = buildRequest(prompt, parameters, false);

        try {
            String response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody.toString())
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            return extractResponse(response);
        } catch (Exception e) {
            log.error("Ollama generation failed: {}", e.getMessage());
            throw new AiProviderException(getProviderName(), model, "Generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateJson(String prompt, Map<String, Object> parameters) {
        log.debug("Ollama generateJson: model={}", model);

        // Append JSON instruction to prompt for models without native JSON mode
        String jsonPrompt = prompt + "\n\nRespond ONLY with valid JSON. No markdown, no explanation.";

        ObjectNode requestBody = buildRequest(jsonPrompt, parameters, true);

        try {
            String response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody.toString())
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            return extractJsonResponse(response);
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama JSON generation failed: {}", e.getMessage());
            throw new AiProviderException(getProviderName(), model, "JSON generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        try {
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Ollama not available: {}", e.getMessage());
            return false;
        }
    }

    // ===========================
    // Private helpers
    // ===========================

    private ObjectNode buildRequest(String prompt, Map<String, Object> parameters, boolean jsonMode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("model", model);
        node.put("prompt", prompt);
        node.put("stream", false);

        if (jsonMode) {
            node.put("format", "json");
        }

        // Apply parameters with defaults for small models
        ObjectNode options = mapper.createObjectNode();
        options.put("temperature", getParam(parameters, "temperature", 0.7));
        options.put("num_predict", getParam(parameters, "maxTokens", 1024));
        options.put("top_p", getParam(parameters, "topP", 0.9));
        node.set("options", options);

        return node;
    }

    private double getParam(Map<String, Object> parameters, String key, double defaultValue) {
        if (parameters != null && parameters.containsKey(key)) {
            Object value = parameters.get(key);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        }
        return defaultValue;
    }

    private int getParam(Map<String, Object> parameters, String key, int defaultValue) {
        if (parameters != null && parameters.containsKey(key)) {
            Object value = parameters.get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
        }
        return defaultValue;
    }

    private String extractResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(responseBody);
            return json.get("response").asText();
        } catch (Exception e) {
            throw new AiProviderException(getProviderName(), model,
                    "Failed to parse Ollama response: " + e.getMessage());
        }
    }

    private String extractJsonResponse(String responseBody) {
        String rawResponse = extractResponse(responseBody);

        // Try to extract JSON from the response (may be wrapped in markdown)
        String json = extractJsonFromString(rawResponse);

        // Validate it's valid JSON
        try {
            new ObjectMapper().readTree(json);
            return json;
        } catch (Exception e) {
            log.warn("AI response is not valid JSON, returning raw: {}", rawResponse);
            return rawResponse;
        }
    }

    private String extractJsonFromString(String text) {
        if (text == null) return "{}";

        // Try to find JSON block in markdown code fences
        int jsonStart = text.indexOf("{");
        int jsonArrayStart = text.indexOf("[");

        // Find the first JSON-like character
        int start = -1;
        if (jsonStart >= 0 && (jsonArrayStart < 0 || jsonStart < jsonArrayStart)) {
            start = jsonStart;
        } else if (jsonArrayStart >= 0) {
            start = jsonArrayStart;
        }

        if (start < 0) {
            return text.trim();
        }

        // Find matching closing bracket
        char openBracket = text.charAt(start);
        char closeBracket = openBracket == '{' ? '}' : ']';
        int depth = 0;
        int end = start;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == openBracket) depth++;
            else if (c == closeBracket) {
                depth--;
                if (depth == 0) {
                    end = i + 1;
                    break;
                }
            }
        }

        return text.substring(start, end);
    }
}
