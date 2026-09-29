package com.cloudsherpa.service.agenticdashboard.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AiTextualToolCallParser {

  private static final int MAX_TEXTUAL_TOOL_CALL_BYTES = 8192;
  private static final String ARGUMENTS = "arguments";
  private static final String TOOL_CALL = "<tool_call>";
  private static final String FUNCTION_CALL = "<function_call>";
  private static final Set<String> ALLOWED_FIELDS = Set.of("name", ARGUMENTS);

  private final ObjectMapper objectMapper;

  public AiTextualToolCallParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public JsonNode parse(String content) {
    if (content == null || content.isBlank()) {
      return null;
    }

    String trimmed = content.trim();
    if (!isTextualToolCallCandidate(trimmed)) {
      return null;
    }

    String normalized = unwrap(trimmed);
    if (normalized.getBytes(StandardCharsets.UTF_8).length > MAX_TEXTUAL_TOOL_CALL_BYTES) {
      throw new IllegalArgumentException("Textual tool call exceeds the maximum allowed size");
    }

    if (!normalized.startsWith("{")) {
      throw new IllegalArgumentException("Textual tool call must contain a JSON object");
    }

    JsonNode payload;
    try {
      payload = objectMapper.readTree(normalized);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Textual tool call is not valid JSON", exception);
    }

    if (payload == null || !payload.isObject()) {
      throw new IllegalArgumentException("Textual tool call must be a JSON object");
    }

    if (!payload.has("name") && !payload.has(ARGUMENTS)) {
      return null;
    }

    validateFields(payload);

    JsonNode nameNode = payload.get("name");
    if (nameNode == null || !nameNode.isTextual() || nameNode.asText().isBlank()) {
      throw new IllegalArgumentException("Textual tool call name is required");
    }

    JsonNode argumentsNode = payload.get(ARGUMENTS);
    if (argumentsNode == null || argumentsNode.isNull()) {
      throw new IllegalArgumentException("Textual tool call arguments are required");
    }

    JsonNode normalizedArguments = normalizeArguments(argumentsNode);

    return objectMapper
        .createObjectNode()
        .put("id", "text-call-" + UUID.randomUUID())
        .put("type", "function")
        .set(
            "function",
            objectMapper
                .createObjectNode()
                .put("name", nameNode.asText())
                .put(ARGUMENTS, toJson(normalizedArguments)));
  }

  private boolean isTextualToolCallCandidate(String value) {
    return value.startsWith("{")
        || value.startsWith("```")
        || value.startsWith(TOOL_CALL)
        || value.startsWith(FUNCTION_CALL);
  }

  private String unwrap(String value) {
    String result = value;

    if (result.startsWith(TOOL_CALL)) {
      if (!result.endsWith("</tool_call>")) {
        throw new IllegalArgumentException("Textual tool-call wrapper is not closed");
      }
      result = result.substring(11, result.length() - 12).trim();
    } else if (result.startsWith(FUNCTION_CALL)) {
      if (!result.endsWith("</function_call>")) {
        throw new IllegalArgumentException("Textual function-call wrapper is not closed");
      }
      result = result.substring(15, result.length() - 16).trim();
    }

    if (result.startsWith("```") && result.endsWith("```")) {
      int firstLineEnd = result.indexOf('\n');
      if (firstLineEnd < 0) {
        throw new IllegalArgumentException("Textual tool-call code block is empty");
      }
      result = result.substring(firstLineEnd + 1, result.length() - 3).trim();
    }

    if (result.contains(TOOL_CALL)
        || result.contains("</tool_call>")
        || result.contains(FUNCTION_CALL)
        || result.contains("</function_call>")) {
      throw new IllegalArgumentException(
          "Textual tool-call content contains unsupported surrounding text");
    }

    return result;
  }

  private void validateFields(JsonNode payload) {
    Iterator<String> fields = payload.fieldNames();
    while (fields.hasNext()) {
      String field = fields.next();
      if (!ALLOWED_FIELDS.contains(field)) {
        throw new IllegalArgumentException("Unknown textual tool-call field: " + field);
      }
    }
  }

  private JsonNode normalizeArguments(JsonNode argumentsNode) {
    JsonNode arguments = argumentsNode;

    if (argumentsNode.isTextual()) {
      try {
        arguments = objectMapper.readTree(argumentsNode.asText());
      } catch (JsonProcessingException exception) {
        throw new IllegalArgumentException(
            "Textual tool call arguments are not valid JSON", exception);
      }
    }

    if (arguments == null || !arguments.isObject()) {
      throw new IllegalArgumentException("Textual tool call arguments must be a JSON object");
    }

    return arguments;
  }

  private String toJson(JsonNode value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to normalize textual tool-call arguments", exception);
    }
  }
}
