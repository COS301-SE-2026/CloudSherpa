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
  private static final int DEFAULT_MAX_TOKENS = 768;

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String model;
  private final int maxTokens;

  public AiModelClient(
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      @Value("${ai.llm.base-url}") String baseUrl,
      @Value("${ai.llm.client-id}") String clientId,
      @Value("${ai.llm.client-secret}") String clientSecret,
      @Value("${ai.llm.model:qwen2.5-coder:14b-instruct}") String model,
      @Value("${ai.llm.max-tokens:768}") int maxTokens) {

    this.objectMapper = objectMapper;
    this.model = model;
    this.maxTokens = maxTokens > 0 ? maxTokens : DEFAULT_MAX_TOKENS;

    this.restClient =
        restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("CF-Access-Client-Id", clientId)
            .defaultHeader("CF-Access-Client-Secret", clientSecret)
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
    request.put("temperature", 0);
    request.put("max_tokens", maxTokens);

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
