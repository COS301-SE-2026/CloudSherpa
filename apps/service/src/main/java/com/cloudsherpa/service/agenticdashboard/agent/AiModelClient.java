package com.cloudsherpa.service.agenticdashboard.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiModelClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(AiModelClient.class);
  private static final String OPENAI_BASE_URL = "https://api.openai.com";
  private static final int DEFAULT_MAX_COMPLETION_TOKENS = 8192;

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String model;
  private final int maxCompletionTokens;

  public AiModelClient(
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      @Value("${OPENAI_API_KEY}") String apiKey,
      @Value("${ai.llm.model:gpt-5.4-mini}") String model,
      @Value("${ai.llm.max-completion-tokens:8192}") int maxCompletionTokens) {

    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("ai.llm.api-key must be configured");
    }

    this.objectMapper = objectMapper;
    this.model = model;
    this.maxCompletionTokens =
        maxCompletionTokens > 0 ? maxCompletionTokens : DEFAULT_MAX_COMPLETION_TOKENS;

    this.restClient =
        restClientBuilder
            .baseUrl(OPENAI_BASE_URL)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
  }

  public JsonNode complete(
      List<Map<String, Object>> messages,
      List<Map<String, Object>> tools,
      boolean requireToolCall) {

    Map<String, Object> request = new java.util.LinkedHashMap<>();
    request.put("model", model);
    request.put("messages", messages);
    request.put("tools", tools);
    request.put("tool_choice", requireToolCall ? "required" : "auto");
    request.put("parallel_tool_calls", false);
    request.put("reasoning_effort", "none");
    request.put("max_completion_tokens", maxCompletionTokens);

    JsonNode response =
        restClient
            .post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(JsonNode.class);

    if (response == null) {
      throw new IllegalStateException("AI model returned an empty response");
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Raw LLM response: {}", response.toPrettyString());
    }

    return response;
  }

  public ObjectMapper objectMapper() {
    return objectMapper;
  }
}
