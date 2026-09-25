package com.cloudsherpa.service.agenticdashboard.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiModelClient {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String model;

  public AiModelClient(
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      @Value("${ai.llm.base-url}") String baseUrl,
      @Value("${ai.llm.client-id}") String clientId,
      @Value("${ai.llm.client-secret}") String clientSecret,
      @Value("${ai.llm.model:qwen2.5-coder:14b-instruct}") String model) {

    this.objectMapper = objectMapper;
    this.model = model;

    this.restClient =
        restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("CF-Access-Client-Id", clientId)
            .defaultHeader("CF-Access-Client-Secret", clientSecret)
            .build();
  }

  public JsonNode complete(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {

    Map<String, Object> request =
        Map.of(
            "model",
            model,
            "messages",
            messages,
            "tools",
            tools,
            "tool_choice",
            "auto",
            "temperature",
            0.1);

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

    System.out.println("=== RAW LLM RESPONSE ===");
    System.out.println(response.toPrettyString());
    return response;
  }

  public ObjectMapper objectMapper() {
    return objectMapper;
  }
}
