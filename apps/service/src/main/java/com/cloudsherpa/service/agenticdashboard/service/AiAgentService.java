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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(AiAgentService.class);

  private static final String ROLE_FIELD = "role";
  private static final String CONTENT_FIELD = "content";
  private static final String ROLE_SYSTEM = "system";
  private static final String ROLE_ASSISTANT = "assistant";
  private static final String ROLE_TOOL = "tool";

  private static final String FUNCTION_FIELD = "function";
  private static final String ARGUMENTS_FIELD = "arguments";
  private static final String TOOL_CALL_ID_FIELD = "tool_call_id";

  private static final String STAGE_DASHBOARD_VERSION_TOOL = "stage_dashboard_version";

  private static final String ACCOUNT_ID_FIELD = "accountId";
  private static final String RESOURCE_ID_FIELD = "resourceId";
  private static final String TITLE_FIELD = "title";
  private static final String JSON_STRING_TYPE = "string";
  private static final String JSON_INTEGER_TYPE = "integer";
  private static final String JSON_MINIMUM_FIELD = "minimum";
  private static final String JSON_FORMAT_FIELD = "format";
  private static final String JSON_OBJECT_TYPE = "object";
  private static final String JSON_PROPERTIES_FIELD = "properties";
  private static final String JSON_REQUIRED_FIELD = "required";
  private static final String WIDGETS_FIELD = "widgets";
  private static final String PLAN_FIELD = "plan";

  private static final String STAGED_SUCCESS_MESSAGE = "Dashboard version staged successfully.";

  private static final class AgentRunState {
    private boolean stageAttempted;
  }

  private AiToolResultDto executeToolCall(
      AiAgentContext context, String toolName, String arguments, AgentRunState state) {

    if (STAGE_DASHBOARD_VERSION_TOOL.equals(toolName)) {
      state.stageAttempted = true;
    }

    return executeTool(context, toolName, arguments);
  }

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
      UUID userId, UUID sessionId, UUID startingDashboardId, String userMessage) {

    AiSession session = aiSessionService.getSession(userId, sessionId);
    validateUserMessage(userMessage);
    initializeSessionVersion(session, userId, sessionId, startingDashboardId);

    AiAgentContext context = new AiAgentContext(userId, sessionId);
    saveMessage(sessionId, AiMessage.AiMessageRole.USER, userMessage);

    List<Map<String, Object>> messages = buildInitialMessages(userId, sessionId);
    AgentRunState state = new AgentRunState();

    for (int round = 0; round < maxToolRounds; round++) {
      AiDashboardPlanResponseDto response =
          processRound(userId, sessionId, context, messages, state);

      if (response != null) {
        return response;
      }
    }

    throw new IllegalStateException("AI agent exceeded maximum tool-call rounds");
  }

  private void validateUserMessage(String userMessage) {
    if (userMessage == null || userMessage.isBlank()) {
      throw new IllegalArgumentException("User message is required");
    }
  }

  private void initializeSessionVersion(
      AiSession session, UUID userId, UUID sessionId, UUID startingDashboardId) {

    if (session.getCurrentVersionId() == null) {
      versionService.createInitialVersion(userId, sessionId, startingDashboardId);
    }
  }

  private List<Map<String, Object>> buildInitialMessages(UUID userId, UUID sessionId) {

    List<Map<String, Object>> messages = new ArrayList<>();

    messages.add(Map.of(ROLE_FIELD, ROLE_SYSTEM, CONTENT_FIELD, SYSTEM_PROMPT));

    messages.add(buildCurrentDashboardContext(userId, sessionId));
    messages.addAll(buildConversation(userId, sessionId));

    return messages;
  }

  private AiDashboardPlanResponseDto processRound(
      UUID userId,
      UUID sessionId,
      AiAgentContext context,
      List<Map<String, Object>> messages,
      AgentRunState state) {

    JsonNode message = requestAssistantMessage(messages);

    JsonNode toolCalls = message.path("tool_calls");
    if (toolCalls.isArray() && !toolCalls.isEmpty()) {
      return processNativeToolCalls(
          userId, sessionId, context, messages, message, toolCalls, state);
    }

    String assistantContent = message.path(CONTENT_FIELD).asText("");
    JsonNode textualToolCall = parseTextualToolCall(assistantContent);

    if (textualToolCall != null) {
      return processTextualToolCall(
          userId, sessionId, context, messages, assistantContent, textualToolCall, state);
    }

    return completeAssistantResponse(userId, sessionId, assistantContent, state);
  }

  private JsonNode requestAssistantMessage(List<Map<String, Object>> messages) {
    JsonNode response = modelClient.complete(messages, toolDefinitions());
    JsonNode choices = response.path("choices");

    if (!choices.isArray() || choices.isEmpty()) {
      throw new IllegalStateException("AI model returned no completion choices");
    }

    JsonNode message = choices.get(0).path("message");

    if (message.isMissingNode()) {
      throw new IllegalStateException("AI model returned no assistant message");
    }

    return message;
  }

  private AiDashboardPlanResponseDto processNativeToolCalls(
      UUID userId,
      UUID sessionId,
      AiAgentContext context,
      List<Map<String, Object>> messages,
      JsonNode message,
      JsonNode toolCalls,
      AgentRunState state) {

    messages.add(objectMapper.convertValue(message, Map.class));

    for (JsonNode toolCall : toolCalls) {
      AiDashboardPlanResponseDto response =
          processNativeToolCall(userId, sessionId, context, messages, toolCall, state);

      if (response != null) {
        return response;
      }
    }

    return null;
  }

  private AiDashboardPlanResponseDto processNativeToolCall(
      UUID userId,
      UUID sessionId,
      AiAgentContext context,
      List<Map<String, Object>> messages,
      JsonNode toolCall,
      AgentRunState state) {

    String toolCallId = requiredJsonText(toolCall, "id");
    JsonNode function = toolCall.path(FUNCTION_FIELD);

    String toolName = requiredJsonText(function, "name");
    String arguments = requiredJsonText(function, ARGUMENTS_FIELD);

    AiToolResultDto toolResult = executeToolCall(context, toolName, arguments, state);

    AiDashboardPlanResponseDto response =
        buildSuccessfulStageResponse(userId, sessionId, toolName, toolResult, state);

    if (response != null) {
      return response;
    }

    messages.add(
        Map.of(
            ROLE_FIELD,
            ROLE_TOOL,
            TOOL_CALL_ID_FIELD,
            toolCallId,
            CONTENT_FIELD,
            toJson(toolResult)));

    return null;
  }

  private AiDashboardPlanResponseDto processTextualToolCall(
      UUID userId,
      UUID sessionId,
      AiAgentContext context,
      List<Map<String, Object>> messages,
      String assistantContent,
      JsonNode textualToolCall,
      AgentRunState state) {

    String toolName = requiredJsonText(textualToolCall, "name");
    JsonNode argumentsNode = textualToolCall.path(ARGUMENTS_FIELD);
    String arguments = argumentsNode.isMissingNode() ? "{}" : argumentsNode.toString();

    AiToolResultDto toolResult = executeToolCall(context, toolName, arguments, state);

    AiDashboardPlanResponseDto response =
        buildSuccessfulStageResponse(userId, sessionId, toolName, toolResult, state);

    if (response != null) {
      return response;
    }

    messages.add(Map.of(ROLE_FIELD, ROLE_ASSISTANT, CONTENT_FIELD, assistantContent));

    messages.add(Map.of(ROLE_FIELD, ROLE_TOOL, CONTENT_FIELD, toJson(toolResult)));

    return null;
  }

  private AiDashboardPlanResponseDto buildSuccessfulStageResponse(
      UUID userId,
      UUID sessionId,
      String toolName,
      AiToolResultDto toolResult,
      AgentRunState state) {

    if (!STAGE_DASHBOARD_VERSION_TOOL.equals(toolName) || !toolResult.success()) {
      return null;
    }

    saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, STAGED_SUCCESS_MESSAGE);

    aiSessionService.updateLastActivity(userId, sessionId);

    return buildResponse(userId, sessionId, STAGED_SUCCESS_MESSAGE, state.stageAttempted, true);
  }

  private AiDashboardPlanResponseDto completeAssistantResponse(
      UUID userId, UUID sessionId, String assistantMessage, AgentRunState state) {

    if (assistantMessage.isBlank()) {
      throw new IllegalStateException("AI model returned an empty assistant message");
    }

    saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, assistantMessage);

    aiSessionService.updateLastActivity(userId, sessionId);

    return buildResponse(userId, sessionId, assistantMessage, state.stageAttempted, false);
  }

  private AiToolResultDto executeTool(AiAgentContext context, String toolName, String arguments) {

    try {
      JsonNode args = objectMapper.readTree(arguments);

      if (!args.isObject()) {
        throw new IllegalArgumentException("Tool arguments must be a JSON object");
      }

      String result =
          switch (toolName) {
            case "list_cloud_accounts" ->
                objectMapper.writeValueAsString(mcpTools.listCloudAccounts(context));

            case "list_resources" ->
                objectMapper.writeValueAsString(
                    mcpTools.listResources(
                        context,
                        nullableUuid(args, ACCOUNT_ID_FIELD),
                        nullableText(args, "resourceType")));

            case "list_available_metrics" ->
                objectMapper.writeValueAsString(
                    mcpTools.listAvailableMetrics(context, requiredUuid(args, RESOURCE_ID_FIELD)));

            case "list_billing_charges" ->
                objectMapper.writeValueAsString(mcpTools.listBillingCharges(context));

            case STAGE_DASHBOARD_VERSION_TOOL -> {
              JsonNode planNode = requiredNode(args, PLAN_FIELD);

              if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("AI dashboard plan: {}", planNode.toPrettyString());
              }

              JsonNode planForStaging = preparePlanForStaging(planNode);

              DashboardPlanDto plan =
                  objectMapper.treeToValue(planForStaging, DashboardPlanDto.class);

              yield objectMapper.writeValueAsString(mcpTools.stageDashboardVersion(context, plan));
            }

            default -> throw new IllegalArgumentException("Unknown AI tool: " + toolName);
          };

      return AiToolResultDto.success(toolName, result);
    } catch (Exception exception) {
      return AiToolResultDto.failure(toolName, buildToolErrorMessage(toolName, exception));
    }
  }

  private JsonNode preparePlanForStaging(JsonNode planNode) {
    JsonNode planForStaging = planNode.deepCopy();

    JsonNode widgets = planForStaging.path(WIDGETS_FIELD);

    if (widgets.isArray()) {
      for (JsonNode widget : widgets) {
        removeWidgetId(widget);
      }
    }

    return planForStaging;
  }

  private void removeWidgetId(JsonNode widget) {
    if (widget.isObject()) {
      ((ObjectNode) widget).remove("widgetId");
    }
  }

  private String buildToolErrorMessage(String toolName, Exception exception) {

    String message = exception.getMessage();

    if (message == null || message.isBlank()) {
      message = exception.getClass().getSimpleName();
    }

    return "Tool '" + toolName + "' failed: " + message;
  }

  private AiDashboardPlanResponseDto buildResponse(
      UUID userId,
      UUID sessionId,
      String assistantMessage,
      boolean stageAttempted,
      boolean stageSucceeded) {

    var session = aiSessionService.getSession(userId, sessionId);

    if (session.getCurrentVersionId() == null) {
      return new AiDashboardPlanResponseDto(
          sessionId, null, null, stageAttempted, stageSucceeded, assistantMessage, null);
    }

    var response =
        versionService.getVersionResponse(userId, sessionId, session.getCurrentVersionId());

    return new AiDashboardPlanResponseDto(
        sessionId,
        response.versionId(),
        response.version(),
        stageAttempted,
        stageSucceeded,
        assistantMessage,
        response.dashboard());
  }

  private String toJson(Object value) {

    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize AI agent message", exception);
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
      JsonNode arguments = node.path(ARGUMENTS_FIELD);

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
              result.put(ROLE_FIELD, message.getRole().name().toLowerCase());
              result.put(CONTENT_FIELD, message.getContent());
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
        "widgetType", Map.of("type", JSON_STRING_TYPE, "enum", List.of("KPI", "CHART")));

    widgetProperties.put("displayName", Map.of("type", JSON_STRING_TYPE));

    widgetProperties.put("startX", Map.of("type", JSON_INTEGER_TYPE, JSON_MINIMUM_FIELD, 0));

    widgetProperties.put("startY", Map.of("type", JSON_INTEGER_TYPE, JSON_MINIMUM_FIELD, 0));

    widgetProperties.put(
        "width", Map.of("type", JSON_INTEGER_TYPE, JSON_MINIMUM_FIELD, 1, "maximum", 12));

    widgetProperties.put("height", Map.of("type", JSON_INTEGER_TYPE, JSON_MINIMUM_FIELD, 1));

    widgetProperties.put(
        "chartType",
        Map.of("type", JSON_STRING_TYPE, "enum", List.of("gauge_chart", "line_chart")));

    widgetProperties.put(
        "chartColour",
        Map.of(
            "type",
            JSON_STRING_TYPE,
            "enum",
            List.of("chart_1", "chart_2", "chart_3", "chart_4", "chart_5")));

    widgetProperties.put(
        "provider", Map.of("type", JSON_STRING_TYPE, "enum", List.of("AWS", "AZURE", "GCP")));

    widgetProperties.put(TITLE_FIELD, Map.of("type", JSON_STRING_TYPE));

    widgetProperties.put(
        ACCOUNT_ID_FIELD, Map.of("type", JSON_STRING_TYPE, JSON_FORMAT_FIELD, "uuid"));

    widgetProperties.put(
        RESOURCE_ID_FIELD, Map.of("type", JSON_STRING_TYPE, JSON_FORMAT_FIELD, "uuid"));

    widgetProperties.put("metricType", Map.of("type", JSON_STRING_TYPE));
    widgetProperties.put("metricName", Map.of("type", JSON_STRING_TYPE));

    widgetProperties.put(
        "chargeIds", Map.of("type", "array", "items", Map.of("type", JSON_STRING_TYPE)));

    widgetProperties.put(
        "aggregationWindowDays", Map.of("type", JSON_INTEGER_TYPE, JSON_MINIMUM_FIELD, 1));

    Map<String, Object> widgetSchema =
        Map.of(
            "type",
            JSON_OBJECT_TYPE,
            JSON_PROPERTIES_FIELD,
            widgetProperties,
            JSON_REQUIRED_FIELD,
            List.of("widgetType", "displayName", "startX", "startY", "width", "height"));

    Map<String, Object> planProperties =
        Map.of(
            TITLE_FIELD,
            Map.of("type", JSON_STRING_TYPE),
            "description",
            Map.of("type", JSON_STRING_TYPE),
            "timeFrom",
            Map.of("type", JSON_STRING_TYPE),
            "timeTo",
            Map.of("type", JSON_STRING_TYPE),
            "predefinedTime",
            Map.of("type", JSON_STRING_TYPE),
            WIDGETS_FIELD,
            Map.of("type", "array", "items", widgetSchema));

    Map<String, Object> planSchema =
        Map.of(
            "type",
            JSON_OBJECT_TYPE,
            JSON_PROPERTIES_FIELD,
            planProperties,
            JSON_REQUIRED_FIELD,
            List.of(TITLE_FIELD, WIDGETS_FIELD));

    Map<String, Object> stageParameters =
        Map.of(
            "type",
            JSON_OBJECT_TYPE,
            JSON_PROPERTIES_FIELD,
            Map.of("plan", planSchema),
            JSON_REQUIRED_FIELD,
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
                    ACCOUNT_ID_FIELD, Map.of("type", JSON_STRING_TYPE, JSON_FORMAT_FIELD, "uuid")),
                List.of(ACCOUNT_ID_FIELD))));

    tools.add(
        tool(
            "list_available_metrics",
            "List metrics available for a specific resource.",
            objectSchema(
                Map.of(
                    RESOURCE_ID_FIELD, Map.of("type", JSON_STRING_TYPE, JSON_FORMAT_FIELD, "uuid")),
                List.of(RESOURCE_ID_FIELD))));

    tools.add(
        tool(
            "list_billing_charges",
            "List billing charge IDs available to the authenticated user.",
            objectSchema(Map.of())));

    tools.add(
        tool(
            STAGE_DASHBOARD_VERSION_TOOL,
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

  private Map<String, Object> buildCurrentDashboardContext(UUID userId, UUID sessionId) {

    AiSession session = aiSessionService.getSession(userId, sessionId);

    if (session.getCurrentVersionId() == null) {
      return Map.of(
          ROLE_FIELD,
          ROLE_SYSTEM,
          CONTENT_FIELD,
          """
              CURRENT ACTIVE DASHBOARD:
              There is currently no active dashboard version for this session.

              If the user asks you to create a dashboard, discover the required
              resources/metrics and call stage_dashboard_version.
              """);
    }

    DashboardPlanDto dashboard =
        versionService.getDashboardPlan(userId, sessionId, session.getCurrentVersionId());

    try {
      String dashboardJson = objectMapper.writeValueAsString(dashboard);

      return Map.of(
          ROLE_FIELD,
          ROLE_SYSTEM,
          CONTENT_FIELD,
          """
              CURRENT ACTIVE DASHBOARD

              The following dashboard is the active version for this session.

              Treat it as the starting state when the user asks to
              modify, update, add to, remove from, resize, rename, recolour,
              or otherwise change the dashboard.

              If the user asks for a modification, preserve the existing
              dashboard unless the user explicitly asks to change or remove
              something. You may resize widgets if this is requested by the user.
              Upon resize request match the user request to the most relevant widget
              and increase or decrease the widget size as requested. Surrounding widget
              positions may need to be changed to accommodate the modification.

              You MUST call stage_dashboard_version with the COMPLETE resulting
              dashboard. Do not return a textual acknowledgement instead of
              calling the tool.

              Current dashboard:
              %s
              """
              .formatted(dashboardJson));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize current dashboard context", e);
    }
  }

  private Map<String, Object> objectSchema(Map<String, Object> properties) {

    return Map.of("type", JSON_OBJECT_TYPE, JSON_PROPERTIES_FIELD, properties);
  }

  private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {

    return Map.of(
        "type", JSON_OBJECT_TYPE, JSON_PROPERTIES_FIELD, properties, JSON_REQUIRED_FIELD, required);
  }

  private Map<String, Object> tool(
      String name, String description, Map<String, Object> parameters) {

    return Map.of(
        "type",
        FUNCTION_FIELD,
        FUNCTION_FIELD,
        Map.of("name", name, "description", description, "parameters", parameters));
  }

  private static final String SYSTEM_PROMPT =
      """
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

      If stage_dashboard_version fails, use the returned tool error as feedback,
      correct the dashboard plan, and try again when a retry can reasonably fix
      the problem using additional tool calls or feedback to ensure that the new
      version conforms to the requirements. Never claim that a dashboard was staged
      unless the staging tool returned success.

      You must never apply a dashboard. Applying a dashboard is performed by
      the authenticated human user through a separate endpoint.

      Never request, expose, or fabricate credentials, tokens, secrets,
      passwords, or database information.

      Keep generated dashboards concise and relevant to the user's request.
      """;
}
