package com.example.asset.client;

import com.example.asset.config.LlmProperties;
import com.example.common.jackson.JacksonObjectMappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible LLM client (OpenAI, Azure OpenAI, Ollama, local models).
 * Calls POST /v1/chat/completions and returns the assistant message content.
 */
@Component
public class OpenAiCompatibleLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmClient.class);
    private static final ObjectMapper OBJECT_MAPPER = JacksonObjectMappers.standard();

    private final LlmProperties properties;
    private final RestTemplate restTemplate;

    public OpenAiCompatibleLlmClient(LlmProperties properties,
                                 @Qualifier("llmRestTemplate") RestTemplate llmRestTemplate) {
        this.properties = properties;
        this.restTemplate = llmRestTemplate;
    }

    @Override
    public String complete(String userPrompt) {
        if (!properties.isEnabled() || !hasApiKey()) {
            log.warn("LLM is disabled or api-key not set. Set app.llm.enabled=true and app.llm.api-key (or OPENAI_API_KEY).");
            return null;
        }

        String url = properties.getApiUrl().replaceAll("/$", "") + "/chat/completions";

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", getSystemPrompt()),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "max_tokens", properties.getMaxTokens(),
                "temperature", 0.2
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (hasApiKey()) {
            headers.setBearerAuth(properties.getApiKey().trim());
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = OBJECT_MAPPER.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).path("message");
                    String content = message.path("content").asText(null);
                    if (content != null) {
                        return content.trim();
                    }
                }
            }
        } catch (Exception e) {
            log.error("LLM API call failed: {}", e.getMessage(), e);
        }

        return null;
    }

    private boolean hasApiKey() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    private static String getSystemPrompt() {
        return "You are an expert at extracting asset and inventory data from documents (invoices, warranty cards, AMC documents, spec sheets). "
                + "Always respond with a single valid JSON object only, no markdown or explanation. "
                + "Use the exact keys shown in the schema. Use null for any missing value.";
    }
}
