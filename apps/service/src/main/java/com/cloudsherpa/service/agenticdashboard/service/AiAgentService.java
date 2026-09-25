package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiMessage;
import com.cloudsherpa.lib.entities.AiSession;
import com.cloudsherpa.lib.repositories.AiMessageRepository;
import com.cloudsherpa.service.agenticdashboard.agent.AiAgentContext;
import com.cloudsherpa.service.agenticdashboard.agent.AiModelClient;
import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.AiToolResultDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.mcp.McpTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiAgentService {

  private static final int DEFAULT_MAX_TOOL_ROUNDS = 10;

  private final AiSessionService aiSessionService;
  private final AiDashboardVersionService versionService;
  private final AiMessageRepository messageRepository;
  private final AiModelClient modelClient;
  private final McpTools mcpTools;
  private final ObjectMapper objectMapper;
  private final int maxToolRounds;

  public AiAgentService(
      AiSessionService aiSessionService,
      AiDashboardVersionService versionService,
      AiMessageRepository messageRepository,
      AiModelClient modelClient,
      McpTools mcpTools,
      ObjectMapper objectMapper,
      @Value("${ai.llm.max-tool-rounds:10}") int maxToolRounds) {

    this.aiSessionService = aiSessionService;
    this.versionService = versionService;
    this.messageRepository = messageRepository;
    this.modelClient = modelClient;
    this.mcpTools = mcpTools;
    this.objectMapper = objectMapper;

    this.maxToolRounds = maxToolRounds > 0 ? maxToolRounds : DEFAULT_MAX_TOOL_ROUNDS;
  }

  public AiDashboardPlanResponseDto generateDashboardPlan(
      UUID userId, UUID sessionId, String userMessage) {

    aiSessionService.getSession(userId, sessionId);

    if (userMessage == null || userMessage.isBlank()) {

      throw new IllegalArgumentException("User message is required");
    }

    AiAgentContext context = new AiAgentContext(userId, sessionId);

    saveMessage(sessionId, AiMessage.AiMessageRole.USER, userMessage);

    List<Map<String, Object>> messages = new ArrayList<>();

    messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
    messages.add(buildCurrentDashboardContext(userId, sessionId));
    messages.addAll(buildConversation(userId, sessionId));

    for (int round = 0; round < maxToolRounds; round++) {

      JsonNode response = modelClient.complete(messages, toolDefinitions());

      JsonNode choices = response.path("choices");

      if (!choices.isArray() || choices.isEmpty()) {

        throw new IllegalStateException("AI model returned no completion choices");
      }

      JsonNode message = choices.get(0).path("message");

      if (message.isMissingNode()) {
        throw new IllegalStateException("AI model returned no assistant message");
      }

      JsonNode toolCalls = message.path("tool_calls");

      if (toolCalls.isArray() && !toolCalls.isEmpty()) {

        messages.add(objectMapper.convertValue(message, Map.class));

        for (JsonNode toolCall : toolCalls) {

          String toolCallId = requiredJsonText(toolCall, "id");

          JsonNode function = toolCall.path("function");

          String toolName = requiredJsonText(function, "name");

          String arguments = requiredJsonText(function, "arguments");

          AiToolResultDto toolResult = executeTool(context, toolName, arguments);
          if ("stage_dashboard_version".equals(toolName)
              && toolResult.success()) {

            String assistantMessage = "Dashboard version staged successfully.";

            saveMessage(
                sessionId,
                AiMessage.AiMessageRole.ASSISTANT,
                assistantMessage);

            aiSessionService.updateLastActivity(userId, sessionId);

            return buildResponse(
                userId,
                sessionId,
                assistantMessage);
          }
          messages.add(Map.of("role", "tool", "tool_call_id", toolCallId, "content",
              toJson(toolResult)));
        }

        continue;
      }

      String assistantContent = message.path("content").asText("");

      JsonNode textualToolCall = parseTextualToolCall(assistantContent);

      if (textualToolCall != null) {

        String toolName = requiredJsonText(textualToolCall, "name");

        JsonNode argumentsNode = textualToolCall.path("arguments");

        String arguments = argumentsNode.isMissingNode() ? "{}" : argumentsNode.toString();
        AiToolResultDto toolResult = executeTool(context, toolName, arguments);

        if ("stage_dashboard_version".equals(toolName)
            && toolResult.success()) {
          String assistantMessage = "Dashboard version staged successfully.";
          saveMessage(
              sessionId,
              AiMessage.AiMessageRole.ASSISTANT,
              assistantMessage);

          aiSessionService.updateLastActivity(userId, sessionId);

          return buildResponse(
              userId,
              sessionId,
              assistantMessage);
        }

        messages.add(
            Map.of(
                "role",
                "assistant",
                "content",
                assistantContent));

        messages.add(
            Map.of(
                "role",
                "tool",
                "content",
                toJson(toolResult)));

        continue;
      }
      String assistantMessage = message.path("content").asText("");

      if (assistantMessage.isBlank()) {
        throw new IllegalStateException("AI model returned an empty assistant message");
      }

      saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, assistantMessage);

      aiSessionService.updateLastActivity(userId, sessionId);

      return buildResponse(userId, sessionId, assistantMessage);
    }

    throw new IllegalStateException("AI agent exceeded maximum tool-call rounds");
  }

  private AiToolResultDto executeTool(AiAgentContext context, String toolName, String arguments) {

    try {
      JsonNode args = objectMapper.readTree(arguments);

      if (!args.isObject()) {
        throw new IllegalArgumentException("Tool arguments must be a JSON object");
      }

      String result = switch (toolName) {
        case "list_cloud_accounts" -> objectMapper.writeValueAsString(
            mcpTools.listCloudAccounts(context));

        case "list_resources" -> objectMapper.writeValueAsString(
            mcpTools.listResources(
                context, nullableUuid(args, "accountId"), nullableText(args, "resourceType")));

        case "list_available_metrics" -> objectMapper.writeValueAsString(
            mcpTools.listAvailableMetrics(context, requiredUuid(args, "resourceId")));

        case "list_billing_charges" -> objectMapper.writeValueAsString(
            mcpTools.listBillingCharges(context));

        case "stage_dashboard_version" -> {
          JsonNode planNode = requiredNode(args, "plan");

          System.out.println("=== AI DASHBOARD PLAN ===");
          System.out.println(planNode.toPrettyString());

          DashboardPlanDto plan = objectMapper.treeToValue(planNode, DashboardPlanDto.class);

          yield objectMapper.writeValueAsString(
              mcpTools.stageDashboardVersion(context, plan));
        }
        default -> throw new IllegalArgumentException("Unknown AI tool: " + toolName);
      };
      return AiToolResultDto.success(toolName, result);
    } catch (Exception exception) {
      return AiToolResultDto.failure(
          toolName,
          buildToolErrorMessage(toolName, exception));
    }
  }

  private String buildToolErrorMessage(
      String toolName,
      Exception exception) {

    String message = exception.getMessage();

    if (message == null || message.isBlank()) {
      message = exception.getClass().getSimpleName();
    }

    return "Tool '" + toolName + "' failed: " + message;
  }

  private AiDashboardPlanResponseDto buildResponse(
      UUID userId, UUID sessionId, String assistantMessage) {

    var session = aiSessionService.getSession(userId, sessionId);

    if (session.getCurrentVersionId() == null) {

      return new AiDashboardPlanResponseDto(sessionId, null, null, assistantMessage, null);
    }

    var response = versionService.getVersionResponse(userId, sessionId, session.getCurrentVersionId());

    return new AiDashboardPlanResponseDto(
        sessionId,
        response.versionId(),
        response.version(),
        assistantMessage,
        response.dashboard());
  }

  private String toJson(Object value) {

    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException(
          "Failed to serialize AI agent message",
          exception);
    }
  }

  private JsonNode parseTextualToolCall(String content) {

    if (content == null || content.isBlank()) {
      return null;
    }

    String json = content.trim();

    if (json.startsWith("```json") && json.endsWith("```")) {
      json = json.substring(7, json.length() - 3).trim();
    } else if (json.startsWith("```") && json.endsWith("```")) {
      json = json.substring(3, json.length() - 3).trim();
    }

    try {
      JsonNode node = objectMapper.readTree(json);

      if (!node.isObject()) {
        return null;
      }

      JsonNode name = node.path("name");
      JsonNode arguments = node.path("arguments");

      if (!name.isTextual() || name.asText().isBlank() || arguments.isMissingNode()) {
        return null;
      }

      return node;

    } catch (Exception exception) {
      return null;
    }
  }

  private List<Map<String, Object>> buildConversation(UUID userId, UUID sessionId) {

    aiSessionService.getSession(userId, sessionId);

    return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
        .map(
            message -> {
              Map<String, Object> result = new java.util.HashMap<>();
              result.put("role", message.getRole().name().toLowerCase());
              result.put("content", message.getContent());
              return result;
            })
        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  }

  private void saveMessage(UUID sessionId, AiMessage.AiMessageRole role, String content) {

    messageRepository.save(
        AiMessage.builder()
            .messageId(UUID.randomUUID())
            .sessionId(sessionId)
            .role(role)
            .content(content)
            .createdAt(OffsetDateTime.now())
            .build());
  }

  private static JsonNode requiredNode(JsonNode node, String field) {

    JsonNode value = node.path(field);

    if (value.isMissingNode() || value.isNull()) {

      throw new IllegalArgumentException("Missing required tool argument: " + field);
    }

    return value;
  }

  private static String requiredJsonText(JsonNode node, String field) {

    JsonNode value = requiredNode(node, field);

    String text = value.asText();

    if (text.isBlank()) {
      throw new IllegalArgumentException("Tool argument is blank: " + field);
    }

    return text;
  }

  private static String requiredText(JsonNode node, String field) {

    return requiredJsonText(node, field);
  }

  private static UUID requiredUuid(JsonNode node, String field) {

    return UUID.fromString(requiredText(node, field));
  }

  private static UUID nullableUuid(JsonNode node, String field) {

    if (!node.hasNonNull(field) || node.path(field).asText().isBlank()) {

      return null;
    }

    return UUID.fromString(node.path(field).asText());
  }

  private static String nullableText(JsonNode node, String field) {

    if (!node.hasNonNull(field)) {
      return null;
    }

    return node.path(field).asText();
  }

  private List<Map<String, Object>> toolDefinitions() {

    Map<String, Object> widgetProperties = new java.util.LinkedHashMap<>();
    widgetProperties.put(
        "widgetType",
        Map.of(
            "type",
            "string",
            "enum",
            List.of("KPI", "CHART")));

    widgetProperties.put(
        "displayName",
        Map.of("type", "string"));

    widgetProperties.put(
        "startX",
        Map.of(
            "type",
            "integer",
            "minimum",
            0));

    widgetProperties.put(
        "startY",
        Map.of(
            "type",
            "integer",
            "minimum",
            0));

    widgetProperties.put(
        "width",
        Map.of(
            "type",
            "integer",
            "minimum",
            1,
            "maximum",
            12));

    widgetProperties.put(
        "height",
        Map.of(
            "type",
            "integer",
            "minimum",
            1));

    widgetProperties.put(
        "chartType",
        Map.of(
            "type",
            "string",
            "enum",
            List.of(
                "gauge_chart",
                "line_chart")));

    widgetProperties.put(
        "chartColour",
        Map.of(
            "type",
            "string",
            "enum",
            List.of(
                "chart_1",
                "chart_2",
                "chart_3",
                "chart_4",
                "chart_5")));

    widgetProperties.put(
        "provider",
        Map.of(
            "type",
            "string",
            "enum",
            List.of(
                "AWS",
                "AZURE",
                "GCP")));

    widgetProperties.put(
        "title",
        Map.of("type", "string"));

    widgetProperties.put(
        "accountId",
        Map.of(
            "type",
            "string",
            "format",
            "uuid"));

    widgetProperties.put(
        "resourceId",
        Map.of(
            "type",
            "string",
            "format",
            "uuid"));

    widgetProperties.put(
        "metricType",
        Map.of("type", "string"));

    widgetProperties.put(
        "metricName",
        Map.of("type", "string"));

    widgetProperties.put(
        "chargeIds",
        Map.of(
            "type",
            "array",
            "items",
            Map.of(
                "type",
                "string")));

    widgetProperties.put(
        "aggregationWindowDays",
        Map.of(
            "type",
            "integer",
            "minimum",
            1));

    Map<String, Object> widgetSchema = Map.of(
        "type",
        "object",
        "properties",
        widgetProperties,
        "required",
        List.of(
            "widgetType",
            "displayName",
            "startX",
            "startY",
            "width",
            "height"));

    Map<String, Object> planProperties = Map.of(
        "title",
        Map.of("type", "string"),
        "description",
        Map.of("type", "string"),
        "timeFrom",
        Map.of("type", "string"),
        "timeTo",
        Map.of("type", "string"),
        "predefinedTime",
        Map.of("type", "string"),
        "widgets",
        Map.of(
            "type",
            "array",
            "items",
            widgetSchema));

    Map<String, Object> planSchema = Map.of(
        "type",
        "object",
        "properties",
        planProperties,
        "required",
        List.of(
            "title",
            "widgets"));

    Map<String, Object> stageParameters = Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "plan",
            planSchema),
        "required",
        List.of("plan"));

    List<Map<String, Object>> tools = new ArrayList<>();

    tools.add(
        tool(
            "list_cloud_accounts",
            "List cloud accounts available to the authenticated user.",
            objectSchema(Map.of())));

    tools.add(
        tool(
            "list_resources",
            "List all resources belonging to one of the user's cloud accounts. "
                + "Do not guess or invent resource types.",
            objectSchema(
                Map.of(
                    "accountId",
                    Map.of(
                        "type",
                        "string",
                        "format",
                        "uuid")),
                List.of("accountId"))));
    tools.add(
        tool(
            "list_available_metrics",
            "List metrics available for a specific resource.",
            objectSchema(
                Map.of(
                    "resourceId",
                    Map.of(
                        "type",
                        "string",
                        "format",
                        "uuid")),
                List.of("resourceId"))));

    tools.add(
        tool(
            "list_billing_charges",
            "List billing charge IDs available to the authenticated user.",
            objectSchema(Map.of())));

    tools.add(
        tool(
            "stage_dashboard_version",
            """
                Create a new validated staged dashboard version.

                Every invocation creates a new dashboard version.
                Never use this tool merely to acknowledge a request.
                If the user asks to create, modify, update, add, remove, resize,
                rearrange, rename, recolour, or otherwise change a dashboard,
                you MUST call this tool with the complete resulting dashboard plan.

                The plan must represent the entire resulting dashboard, not only
                the changes. This does not apply the dashboard.

                The plan must use the exact CloudSherpa dashboard schema.

                Dashboard fields:
                - title: required string
                - description: optional string
                - timeFrom: optional ISO-8601 datetime string
                - timeTo: optional ISO-8601 datetime string
                - predefinedTime: optional CloudSherpa predefined time value
                - widgets: required array

                Widget fields:
                - widgetType: required, either "KPI" or "CHART"
                - displayName: required string
                - startX: required integer, zero or greater
                - startY: required integer, zero or greater
                - width: required integer from 1 to 12
                - height: required positive integer

                For CHART widgets:
                - chartType: required, either "gauge_chart" or "line_chart"
                - chartColour: optional, one of "chart_1", "chart_2", "chart_3", "chart_4", "chart_5"
                - provider: required, one of "AWS", "AZURE", "GCP"
                - accountId: required UUID returned by CloudSherpa tools
                - resourceId: required UUID returned by CloudSherpa tools
                - metricType: required metric type returned by CloudSherpa tools
                - metricName: required metric name returned by CloudSherpa tools
                - title: optional string

                For KPI widgets:
                - chargeIds: required array of billing charge IDs returned by CloudSherpa tools
                - aggregationWindowDays: required positive integer

                Never invent account IDs, resource IDs, metric names, metric types, or billing charge IDs.
                Use only identifiers and metric values returned by CloudSherpa tools.
                """,
            stageParameters));

    return tools;
  }

  private Map<String, Object> buildCurrentDashboardContext(
      UUID userId,
      UUID sessionId) {

    AiSession session = aiSessionService.getSession(userId, sessionId);

    if (session.getCurrentVersionId() == null) {
      return Map.of(
          "role", "system",
          "content", """
              CURRENT STAGED DASHBOARD:
              There is currently no staged dashboard for this session.

              If the user asks you to create a dashboard, discover the required
              resources/metrics and call stage_dashboard_version.
              """);
    }

    DashboardPlanDto dashboard = versionService.getDashboardPlan(
        userId,
        sessionId,
        session.getCurrentVersionId());

    try {
      String dashboardJson = objectMapper.writeValueAsString(dashboard);

      return Map.of(
          "role", "system",
          "content", """
              CURRENT STAGED DASHBOARD

              The following dashboard is the current staged version for this
              session. Treat it as the starting state when the user asks to
              modify, update, add to, remove from, resize, rename, recolour,
              or otherwise change the dashboard.

              If the user asks for a modification, preserve the existing
              dashboard unless the user explicitly asks to change or remove
              something.

              You MUST call stage_dashboard_version with the COMPLETE resulting
              dashboard. Do not return a textual acknowledgement instead of
              calling the tool.

              Current dashboard:
              %s
              """.formatted(dashboardJson));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(
          "Failed to serialize current dashboard context", e);
    }
  }

  private Map<String, Object> objectSchema(Map<String, Object> properties) {

    return Map.of("type", "object", "properties", properties);
  }

  private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {

    return Map.of("type", "object", "properties", properties, "required", required);
  }

  private Map<String, Object> tool(
      String name, String description, Map<String, Object> parameters) {

    return Map.of(
        "type",
        "function",
        "function",
        Map.of("name", name, "description", description, "parameters", parameters));
  }

  private static final String SYSTEM_PROMPT = """
      You are the CloudSherpa Dashboard Construction Agent.

      Construct dashboards only from data discovered through CloudSherpa tools.

      Never invent account IDs, resource IDs, metric names, metric identifiers,
      or billing charge IDs.

      Before staging a dashboard:
      1. Discover the relevant cloud accounts.
      2. Discover the relevant resources.
      3. Discover available metrics for selected resources.
      4. Discover billing charges when a KPI is required.
      5. Use only identifiers returned by CloudSherpa tools.

      You may stage a dashboard version.

      You must never apply a dashboard. Applying a dashboard is performed by
      the authenticated human user through a separate endpoint.

      Never request, expose, or fabricate credentials, tokens, secrets,
      passwords, or database information.

      Keep generated dashboards concise and relevant to the user's request.
      """;
}
