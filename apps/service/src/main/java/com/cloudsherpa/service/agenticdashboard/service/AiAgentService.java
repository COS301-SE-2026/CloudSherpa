package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiMessage;
import com.cloudsherpa.lib.entities.AiSession;
import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.lib.repositories.AiMessageRepository;
import com.cloudsherpa.service.agenticdashboard.agent.AiAgentContext;
import com.cloudsherpa.service.agenticdashboard.agent.AiModelClient;
import com.cloudsherpa.service.agenticdashboard.agent.AiTextualToolCallParser;
import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.AiToolResultDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import com.cloudsherpa.service.agenticdashboard.mcp.McpTools;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.AddChartWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.AddKpiWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateChartWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateDashboardToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateKpiWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateWidgetLayoutToolDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiAgentService {

  private static final int DEFAULT_MAX_TOOL_ROUNDS = 50;
  private static final int DEFAULT_MAX_TOOL_CALLS = 30;
  private static final int MAX_TOOL_ARGUMENT_BYTES = 8192;
  private static final int MAX_PROTOCOL_REPAIR_ATTEMPTS = 5;

  private static final String ROLE_FIELD = "role";
  private static final String CONTENT_FIELD = "content";
  private static final String ROLE_SYSTEM = "system";
  private static final String ROLE_USER = "user";
  private static final String ROLE_ASSISTANT = "assistant";
  private static final String ROLE_TOOL = "tool";
  private static final String TOOL_CALLS_FIELD = "tool_calls";
  private static final String FUNCTION_FIELD = "function";
  private static final String NAME_FIELD = "name";
  private static final String ARGUMENTS_FIELD = "arguments";
  private static final String TOOL_CALL_ID_FIELD = "tool_call_id";
  private static final String TYPE_FIELD = "type";

  private static final String FIND_ACCOUNTS_TOOL = "find_cloud_accounts";
  private static final String FIND_RESOURCES_TOOL = "find_resources";
  private static final String FIND_METRICS_TOOL = "find_available_metrics";
  private static final String FIND_CHARGES_TOOL = "find_billing_charges";
  private static final String ADD_CHART_TOOL = "add_chart_widget";
  private static final String ADD_KPI_TOOL = "add_kpi_widget";
  private static final String UPDATE_LAYOUT_TOOL = "update_widget_layout";
  private static final String UPDATE_CHART_TOOL = "update_chart_widget";
  private static final String UPDATE_KPI_TOOL = "update_kpi_widget";
  private static final String DELETE_WIDGET_TOOL = "delete_widget";
  private static final String CLEAR_DASHBOARD_TOOL = "clear_working_dashboard";
  private static final String DISCARD_DASHBOARD_TOOL = "discard_working_changes";
  private static final String UPDATE_DASHBOARD_TOOL = "update_dashboard";
  private static final String COMMIT_TOOL = "commit_dashboard_changes";

  private static final String MESSAGE_TOOL_ERROR_PREFIX = "Tool error: ";
  private static final String COMMIT_SUCCESS_MESSAGE = "Dashboard changes committed successfully.";
  private static final String SAFETY_STOP_MESSAGE =
      "Dashboard agent reached a safety limit. "
          + "The working changes remain staged and can be continued in the same session.";
  private static final String REPEATED_TOOL_STOP_MESSAGE =
      "Dashboard agent stopped due to repeatedly requesting the same action. "
          + "No duplicate dashboard mutation was applied.";
  private static final int MAX_REPEATED_TOOL_CALLS = 1;

  private static final String SYSTEM_PROMPT =
      """
      You are the CloudSherpa Dashboard Construction Agent.

      Work only with the authenticated user's dashboard data exposed by tools.
      Never invent IDs, metric names, charge IDs, provider/account identity, or credentials.

      Use native OpenAI-compatible tool calls.
      Make exactly one tool call per assistant turn.
      Do not emit textual JSON tool calls and do not emit a complete dashboard plan.

      For changes to an existing dashboard, update the working dashboard with the smallest necessary mutation.
      For a completely new dashboard, clear_working_dashboard first, then add only the requested widgets.
      If the user asks to undo all uncommitted changes, use discard_working_changes.

      The backend owns widget IDs, provider/account identity, metric type, validation,
      defaults, and layout collision handling.

      Use discovery tools before selecting resources, metrics, or billing charges unless the
      needed identifier is already visible in the current dashboard context.

      A successful tool result is authoritative.
      Do not repeat an identical successful tool call.
      After a mutation succeeds, decide whether another distinct mutation is still required.
      Do not rediscover information that is already available in the current dashboard context
      or in a previous successful tool result.

      After all requested changes are complete, call commit_dashboard_changes exactly once.
      Do not call commit_dashboard_changes until all requested changes are complete.
      Do not claim that changes were committed unless that tool succeeds.

      Never call discard_working_changes unless the user explicitly asks to undo,
      discard, revert, or throw away uncommitted dashboard changes.

      Never use discard_working_changes as a recovery mechanism for a failed tool call.

      After a successful mutation, continue only when another distinct user-requested
      change is still required.

      If requested dashboard changes have been successfully made, commit them.
      A successful commit_dashboard_changes call is terminal; do not request another tool.
      The working dashboard is a private server-side draft.
      The authenticated human user is the only actor that can apply a committed version
      to a production dashboard.

      Keep responses concise and do not reproduce the dashboard JSON.
      """;

  private final AiSessionService aiSessionService;
  private final AiDashboardVersionService versionService;
  private final AiMessageRepository messageRepository;
  private final AiModelClient modelClient;
  private final AiTextualToolCallParser textualToolCallParser;
  private final McpTools mcpTools;
  private final ObjectMapper objectMapper;
  private final int maxToolRounds;
  private final int maxToolCalls;
  private final Map<UUID, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();
  private final List<Map<String, Object>> toolDefinitions;

  public AiAgentService(
      AiSessionService aiSessionService,
      AiDashboardVersionService versionService,
      AiMessageRepository messageRepository,
      AiModelClient modelClient,
      AiTextualToolCallParser textualToolCallParser,
      McpTools mcpTools,
      ObjectMapper objectMapper,
      @Value("${ai.llm.max-tool-rounds:50}") int maxToolRounds,
      @Value("${ai.llm.max-tool-calls:50}") int maxToolCalls) {

    this.aiSessionService = aiSessionService;
    this.versionService = versionService;
    this.messageRepository = messageRepository;
    this.modelClient = modelClient;
    this.textualToolCallParser = textualToolCallParser;
    this.mcpTools = mcpTools;
    this.objectMapper = objectMapper;
    this.maxToolRounds = maxToolRounds > 0 ? maxToolRounds : DEFAULT_MAX_TOOL_ROUNDS;
    this.maxToolCalls = maxToolCalls > 0 ? maxToolCalls : DEFAULT_MAX_TOOL_CALLS;
    this.toolDefinitions = toolDefinitions();
  }

  public AiDashboardPlanResponseDto generateDashboardPlan(
      UUID userId, UUID sessionId, UUID startingDashboardId, String userMessage) {

    validateUserMessage(userMessage);
    ReentrantLock lock = sessionLocks.computeIfAbsent(sessionId, key -> new ReentrantLock());
    lock.lock();

    try {
      AiSession session = aiSessionService.getSession(userId, sessionId);
      initializeSessionVersion(session, userId, sessionId, startingDashboardId);
      AiAgentContext context = new AiAgentContext(userId, sessionId);
      saveMessage(sessionId, AiMessage.AiMessageRole.USER, userMessage);

      AgentRunState state = new AgentRunState(userMessage);
      List<Map<String, Object>> history = new ArrayList<>();

      for (int round = 0; round < maxToolRounds; round++) {
        if (state.toolCallCount >= maxToolCalls) {
          return safetyStop(userId, sessionId, state);
        }

        List<Map<String, Object>> messages =
            buildRequestMessages(userId, sessionId, userMessage, history);
        JsonNode message;
        try {
          message =
              requestAssistantMessage(
                  messages, state.protocolRepairAttempts > 0 || state.noToolRepairAttempts > 0);
        } catch (RuntimeException exception) {
          AiDashboardPlanResponseDto fallback = autoCommitWorkingDraft(userId, sessionId, state);

          if (fallback != null) {
            return fallback;
          }

          throw exception;
        }

        JsonNode toolCalls = message.path(TOOL_CALLS_FIELD);

        if (toolCalls.isArray() && !toolCalls.isEmpty()) {
          if (toolCalls.size() != 1) {
            String feedback =
                "The previous response returned multiple native tool calls. "
                    + "Only one native CloudSherpa tool call is allowed per turn. "
                    + "Choose the single most appropriate tool call and try again.";
            if (registerProtocolRepair(state)) {
              history.addAll(buildProtocolFeedback(feedback));
              continue;
            }
            return protocolStop(userId, sessionId, state, feedback);
          }

          JsonNode toolCall = toolCalls.get(0);
          String signature = buildToolCallSignature(toolCall);
          ToolCallResult cachedResult = state.completedToolCalls.get(signature);
          if (cachedResult != null) {
            int repeatedCount = state.repeatedToolCalls.merge(signature, 1, Integer::sum);
            if (repeatedCount > MAX_REPEATED_TOOL_CALLS) {
              AiDashboardPlanResponseDto fallback =
                  autoCommitWorkingDraft(userId, sessionId, state);
              if (fallback != null) {
                return fallback;
              }
              return safetyStopWithMessage(userId, sessionId, state, REPEATED_TOOL_STOP_MESSAGE);
            }

            history.addAll(buildToolContinuation(toolCall, cachedResult.toolResult()));
            history.add(
                Map.of(
                    ROLE_FIELD,
                    ROLE_SYSTEM,
                    CONTENT_FIELD,
                    "This exact tool call already succeeded. Do not repeat it. "
                        + "Either perform a different necessary action or commit the completed changes."));
            continue;
          }

          state.toolCallCount++;
          ToolCallResult result = executeNativeToolCall(context, toolCall, state);
          System.out.println(
              "AI tool result: name="
                  + result.toolName()
                  + ", success="
                  + result.toolResult().success()
                  + ", error="
                  + result.toolResult().error()
                  + ", result="
                  + result.toolResult().result());
          if (result.toolResult().success()) {
            state.completedToolCalls.put(signature, result);
          }

          if (COMMIT_TOOL.equals(result.toolName()) && result.toolResult().success()) {
            saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, COMMIT_SUCCESS_MESSAGE);
            return buildResponse(
                userId, sessionId, COMMIT_SUCCESS_MESSAGE, state.commitAttempted, true);
          }

          history.addAll(buildToolContinuation(toolCall, result.toolResult()));
          continue;
        }

        String assistantContent = message.path(CONTENT_FIELD).asText("").trim();
        JsonNode textualToolCall;
        try {
          textualToolCall = textualToolCallParser.parse(assistantContent);
        } catch (IllegalArgumentException exception) {
          String feedback =
              "The textual tool call was malformed: "
                  + exception.getMessage()
                  + ". Return exactly one tool call with a valid tool name and JSON object arguments.";
          if (registerProtocolRepair(state)) {
            history.addAll(buildProtocolFeedback(feedback));
            continue;
          }
          return protocolStop(userId, sessionId, state, feedback);
        }

        if (textualToolCall != null) {
          String signature = buildToolCallSignature(textualToolCall);
          ToolCallResult cachedResult = state.completedToolCalls.get(signature);
          if (cachedResult != null) {
            int repeatedCount = state.repeatedToolCalls.merge(signature, 1, Integer::sum);
            if (repeatedCount > MAX_REPEATED_TOOL_CALLS) {
              AiDashboardPlanResponseDto fallback =
                  autoCommitWorkingDraft(userId, sessionId, state);

              if (fallback != null) {
                return fallback;
              }

              return safetyStopWithMessage(userId, sessionId, state, REPEATED_TOOL_STOP_MESSAGE);
            }
            history.addAll(buildToolContinuation(textualToolCall, cachedResult.toolResult()));
            history.add(
                Map.of(
                    ROLE_FIELD,
                    ROLE_SYSTEM,
                    CONTENT_FIELD,
                    "This exact tool call already succeeded. Do not repeat it. "
                        + "Either perform a different necessary action or commit the completed changes."));
            continue;
          }

          state.toolCallCount++;
          ToolCallResult result = executeNativeToolCall(context, textualToolCall, state);
          System.out.println(
              "AI tool result: name="
                  + result.toolName()
                  + ", success="
                  + result.toolResult().success()
                  + ", error="
                  + result.toolResult().error()
                  + ", result="
                  + result.toolResult().result());

          if (result.toolResult().success()) {
            state.completedToolCalls.put(signature, result);
          }
          if (COMMIT_TOOL.equals(result.toolName()) && result.toolResult().success()) {
            saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, COMMIT_SUCCESS_MESSAGE);
            return buildResponse(
                userId, sessionId, COMMIT_SUCCESS_MESSAGE, state.commitAttempted, true);
          }

          history.addAll(buildToolContinuation(textualToolCall, result.toolResult()));
          continue;
        }

        if (assistantContent.isBlank()) {
          String feedback =
              "The previous response was empty. Either make exactly one native CloudSherpa tool call "
                  + "or provide a concise final answer.";
          if (registerProtocolRepair(state)) {
            history.addAll(buildProtocolFeedback(feedback));
            continue;
          }
          return protocolStop(userId, sessionId, state, feedback);
        }

        if (state.noToolRepairAttempts == 0 && likelyRequiresTool(userMessage)) {
          state.noToolRepairAttempts++;
          history.addAll(
              List.of(
                  Map.of(
                      ROLE_FIELD,
                      ROLE_SYSTEM,
                      CONTENT_FIELD,
                      "Your previous response did not call a tool. "
                          + "Use exactly one native CloudSherpa tool now. "
                          + "Do not emit JSON tool-call text or a dashboard plan.")));
          continue;
        }

        saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, assistantContent);
        aiSessionService.updateLastActivity(userId, sessionId);
        return buildResponse(userId, sessionId, assistantContent, state.mutationAttempted, false);
      }

      return safetyStop(userId, sessionId, state);
    } finally {
      lock.unlock();
      sessionLocks.remove(sessionId, lock);
    }
  }

  private void validateUserMessage(String userMessage) {
    if (userMessage == null || userMessage.isBlank()) {
      throw new IllegalArgumentException("User message is required");
    }
    if (userMessage.length() > 4000) {
      throw new IllegalArgumentException("User message must not exceed 4000 characters");
    }
  }

  private void initializeSessionVersion(
      AiSession session, UUID userId, UUID sessionId, UUID startingDashboardId) {
    if (session.getCurrentVersionId() == null) {
      versionService.createInitialVersion(userId, sessionId, startingDashboardId);
    }
  }

  private List<Map<String, Object>> buildRequestMessages(
      UUID userId, UUID sessionId, String userMessage, List<Map<String, Object>> history) {

    List<Map<String, Object>> messages = new ArrayList<>();
    messages.add(Map.of(ROLE_FIELD, ROLE_SYSTEM, CONTENT_FIELD, SYSTEM_PROMPT));
    messages.add(buildWorkingDashboardContext(userId, sessionId));
    messages.add(Map.of(ROLE_FIELD, ROLE_USER, CONTENT_FIELD, userMessage));
    messages.addAll(history);
    return messages;
  }

  private Map<String, Object> buildWorkingDashboardContext(UUID userId, UUID sessionId) {
    DashboardPlanDto dashboard = versionService.getWorkingDashboard(userId, sessionId);
    boolean draft = versionService.hasWorkingDraft(userId, sessionId);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("state", draft ? "working_draft" : "committed_dashboard");
    payload.put("title", dashboard.title());
    payload.put("description", dashboard.description());
    payload.put("timeFrom", dashboard.timeFrom());
    payload.put("timeTo", dashboard.timeTo());
    payload.put("predefinedTime", dashboard.predefinedTime());

    List<Map<String, Object>> widgets =
        dashboard.widgets().stream().map(this::compactWidget).toList();
    payload.put("widgets", widgets);

    return Map.of(
        ROLE_FIELD,
        ROLE_SYSTEM,
        CONTENT_FIELD,
        "You are editing the server-side dashboard shown below. "
            + "The backend owns IDs, provider/account identity, metric type, validation, "
            + "and layout collision handling. Use exact widget IDs when editing.\n"
            + "Current dashboard state:\n"
            + toJson(payload));
  }

  private Map<String, Object> compactWidget(DashboardPlanWidgetDto widget) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("widgetId", widget.widgetId());
    result.put("type", widget.widgetType());
    result.put("name", widget.displayName());
    result.put("x", widget.startX());
    result.put("y", widget.startY());
    result.put("w", widget.width());
    result.put("h", widget.height());

    if (widget.widgetType() == TypeEnum.CHART) {
      result.put("resourceId", widget.resourceId());
      result.put("metricName", widget.metricName());
      result.put("provider", widget.provider());
      result.put("accountId", widget.accountId());
      result.put("chartType", widget.chartType());
      result.put("chartColour", widget.chartColour());
    } else {
      result.put("chargeIds", widget.chargeIds());
      result.put("aggregationWindowDays", widget.aggregationWindowDays());
    }

    return result;
  }

  private AiDashboardPlanResponseDto autoCommitWorkingDraft(
      UUID userId, UUID sessionId, AgentRunState state) {

    if (!versionService.hasUncommittedChanges(userId, sessionId)) {
      return null;
    }

    try {
      versionService.commitWorkingDashboard(userId, sessionId);

      String message =
          "The dashboard changes were staged automatically after the agent stopped before committing.";

      saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, message);
      aiSessionService.updateLastActivity(userId, sessionId);

      return buildResponse(userId, sessionId, message, true, true);
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private JsonNode requestAssistantMessage(
      List<Map<String, Object>> messages, boolean stateRequiresNativeToolCall) {
    JsonNode response =
        modelClient.complete(messages, toolDefinitions, stateRequiresNativeToolCall);
    JsonNode choices = response.path("choices");

    if (!choices.isArray() || choices.isEmpty()) {
      throw new IllegalStateException("AI model returned no completion choices");
    }

    JsonNode message = choices.get(0).path("message");
    if (message.isMissingNode() || !message.isObject()) {
      throw new IllegalStateException("AI model returned no assistant message");
    }

    return message;
  }

  private ToolCallResult executeNativeToolCall(
      AiAgentContext context, JsonNode toolCall, AgentRunState state) {
    String toolCallId = requiredJsonText(toolCall, "id");
    JsonNode function = toolCall.path(FUNCTION_FIELD);
    String toolName = requiredJsonText(function, NAME_FIELD);
    String arguments = requiredJsonText(function, ARGUMENTS_FIELD);

    if (arguments.length() > MAX_TOOL_ARGUMENT_BYTES) {
      return new ToolCallResult(
          toolCallId,
          toolName,
          AiToolResultDto.failure(toolName, "Tool arguments exceed the maximum allowed size"));
    }

    if (COMMIT_TOOL.equals(toolName)) {
      state.commitAttempted = true;
    }
    AiToolResultDto toolResult = executeTool(context, toolName, arguments);
    if (isMutatingTool(toolName) && toolResult.success()) {
      state.mutationAttempted = true;
    }
    return new ToolCallResult(toolCallId, toolName, toolResult);
  }

  private boolean isMutatingTool(String toolName) {
    return ADD_CHART_TOOL.equals(toolName)
        || ADD_KPI_TOOL.equals(toolName)
        || UPDATE_LAYOUT_TOOL.equals(toolName)
        || UPDATE_CHART_TOOL.equals(toolName)
        || UPDATE_KPI_TOOL.equals(toolName)
        || DELETE_WIDGET_TOOL.equals(toolName)
        || CLEAR_DASHBOARD_TOOL.equals(toolName)
        || DISCARD_DASHBOARD_TOOL.equals(toolName)
        || UPDATE_DASHBOARD_TOOL.equals(toolName)
        || COMMIT_TOOL.equals(toolName);
  }

  private AiToolResultDto executeTool(AiAgentContext context, String toolName, String arguments) {

    try {
      JsonNode args = objectMapper.readTree(arguments);
      if (!args.isObject()) {
        throw new IllegalArgumentException("Tool arguments must be a JSON object");
      }

      return switch (toolName) {
        case FIND_ACCOUNTS_TOOL -> {
          validateProperties(args, Set.of("query", "limit"));
          yield success(
              toolName,
              mcpTools.findCloudAccounts(
                  context, optionalText(args, "query"), optionalInteger(args, "limit")));
        }
        case FIND_RESOURCES_TOOL -> {
          validateProperties(args, Set.of("accountId", "query", "resourceType", "limit"));
          yield success(
              toolName,
              mcpTools.findResources(
                  context,
                  requiredUuid(args, "accountId"),
                  optionalText(args, "query"),
                  optionalText(args, "resourceType"),
                  optionalInteger(args, "limit")));
        }
        case FIND_METRICS_TOOL -> {
          validateProperties(args, Set.of("resourceId", "query", "limit"));
          yield success(
              toolName,
              mcpTools.findAvailableMetrics(
                  context,
                  requiredUuid(args, "resourceId"),
                  optionalText(args, "query"),
                  optionalInteger(args, "limit")));
        }
        case FIND_CHARGES_TOOL -> {
          validateProperties(args, Set.of("query", "limit"));
          yield success(
              toolName,
              mcpTools.findBillingCharges(
                  context, optionalText(args, "query"), optionalInteger(args, "limit")));
        }
        case ADD_CHART_TOOL -> {
          validateProperties(
              args,
              Set.of(
                  "displayName",
                  "resourceId",
                  "metricName",
                  "chartType",
                  "chartColour",
                  "startX",
                  "startY",
                  "width",
                  "height"));
          AddChartWidgetToolDto request =
              objectMapper.treeToValue(args, AddChartWidgetToolDto.class);
          yield success(toolName, mcpTools.addChartWidget(context, request));
        }
        case ADD_KPI_TOOL -> {
          validateProperties(
              args,
              Set.of(
                  "displayName",
                  "chargeIds",
                  "aggregationWindowDays",
                  "startX",
                  "startY",
                  "width",
                  "height"));
          AddKpiWidgetToolDto request = objectMapper.treeToValue(args, AddKpiWidgetToolDto.class);
          yield success(toolName, mcpTools.addKpiWidget(context, request));
        }
        case UPDATE_LAYOUT_TOOL -> {
          validateProperties(args, Set.of("widgetId", "startX", "startY", "width", "height"));
          UpdateWidgetLayoutToolDto request =
              objectMapper.treeToValue(args, UpdateWidgetLayoutToolDto.class);
          yield success(toolName, mcpTools.updateWidgetLayout(context, request));
        }
        case UPDATE_CHART_TOOL -> {
          validateProperties(
              args,
              Set.of(
                  "widgetId",
                  "displayName",
                  "chartType",
                  "chartColour",
                  "resourceId",
                  "metricName"));
          UpdateChartWidgetToolDto request =
              objectMapper.treeToValue(args, UpdateChartWidgetToolDto.class);
          yield success(toolName, mcpTools.updateChartWidget(context, request));
        }
        case UPDATE_KPI_TOOL -> {
          validateProperties(
              args, Set.of("widgetId", "displayName", "chargeIds", "aggregationWindowDays"));
          UpdateKpiWidgetToolDto request =
              objectMapper.treeToValue(args, UpdateKpiWidgetToolDto.class);
          yield success(toolName, mcpTools.updateKpiWidget(context, request));
        }
        case DELETE_WIDGET_TOOL -> {
          validateProperties(args, Set.of("widgetId"));
          yield success(toolName, mcpTools.deleteWidget(context, requiredUuid(args, "widgetId")));
        }
        case CLEAR_DASHBOARD_TOOL -> {
          validateProperties(args, Set.of());
          yield success(toolName, mcpTools.clearWorkingDashboard(context));
        }
        case DISCARD_DASHBOARD_TOOL -> {
          validateProperties(args, Set.of());
          yield success(toolName, mcpTools.discardWorkingDashboard(context));
        }
        case UPDATE_DASHBOARD_TOOL -> {
          validateProperties(
              args, Set.of("title", "description", "timeFrom", "timeTo", "predefinedTime"));
          UpdateDashboardToolDto request =
              objectMapper.treeToValue(args, UpdateDashboardToolDto.class);
          yield success(toolName, mcpTools.updateDashboard(context, request));
        }
        case COMMIT_TOOL -> {
          validateProperties(args, Set.of());
          yield success(toolName, mcpTools.commitDashboardChanges(context));
        }
        default -> AiToolResultDto.failure(toolName, "Unknown AI tool: " + toolName);
      };
    } catch (Exception exception) {
      return AiToolResultDto.failure(toolName, buildToolErrorMessage(toolName, exception));
    }
  }

  private AiToolResultDto success(String toolName, Object result) {
    return AiToolResultDto.success(toolName, toJson(result));
  }

  private List<Map<String, Object>> buildToolContinuation(
      JsonNode toolCall, AiToolResultDto toolResult) {

    String toolCallId = requiredJsonText(toolCall, "id");
    Map<String, Object> assistantMessage = objectMapper.convertValue(toolCall, Map.class);

    Map<String, Object> assistantWrapper =
        Map.of(ROLE_FIELD, ROLE_ASSISTANT, TOOL_CALLS_FIELD, List.of(assistantMessage));

    Map<String, Object> toolMessage =
        Map.of(
            ROLE_FIELD,
            ROLE_TOOL,
            TOOL_CALL_ID_FIELD,
            toolCallId,
            CONTENT_FIELD,
            toJson(toolResult));

    return List.of(assistantWrapper, toolMessage);
  }

  private AiDashboardPlanResponseDto buildResponse(
      UUID userId,
      UUID sessionId,
      String assistantMessage,
      boolean stageAttempted,
      boolean stageSucceeded) {

    AiSession session = aiSessionService.getSession(userId, sessionId);
    DashboardPlanDto dashboard = versionService.getWorkingDashboard(userId, sessionId);

    if (session.getCurrentVersionId() == null) {
      return new AiDashboardPlanResponseDto(
          sessionId, null, null, stageAttempted, stageSucceeded, assistantMessage, dashboard);
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
        dashboard);
  }

  private AiDashboardPlanResponseDto safetyStop(UUID userId, UUID sessionId, AgentRunState state) {
    AiDashboardPlanResponseDto fallback = autoCommitWorkingDraft(userId, sessionId, state);

    if (fallback != null) {
      return fallback;
    }

    saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, SAFETY_STOP_MESSAGE);
    aiSessionService.updateLastActivity(userId, sessionId);
    return buildResponse(userId, sessionId, SAFETY_STOP_MESSAGE, state.mutationAttempted, false);
  }

  private AiDashboardPlanResponseDto protocolStop(
      UUID userId, UUID sessionId, AgentRunState state, String message) {
    AiDashboardPlanResponseDto fallback = autoCommitWorkingDraft(userId, sessionId, state);

    if (fallback != null) {
      return fallback;
    }

    saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, message);
    aiSessionService.updateLastActivity(userId, sessionId);
    return buildResponse(userId, sessionId, message, state.mutationAttempted, false);
  }

  private AiDashboardPlanResponseDto safetyStopWithMessage(
      UUID userId, UUID sessionId, AgentRunState state, String message) {
    AiDashboardPlanResponseDto fallback = autoCommitWorkingDraft(userId, sessionId, state);

    if (fallback != null) {
      return fallback;
    }

    saveMessage(sessionId, AiMessage.AiMessageRole.ASSISTANT, message);
    aiSessionService.updateLastActivity(userId, sessionId);
    return buildResponse(userId, sessionId, message, state.mutationAttempted, false);
  }

  private List<Map<String, Object>> buildProtocolFeedback(String feedback) {
    return List.of(
        Map.of(
            ROLE_FIELD,
            ROLE_SYSTEM,
            CONTENT_FIELD,
            "Protocol feedback: "
                + feedback
                + " This is a repair attempt; continue the same user request."));
  }

  private boolean registerProtocolRepair(AgentRunState state) {
    if (state.protocolRepairAttempts >= MAX_PROTOCOL_REPAIR_ATTEMPTS) {
      return false;
    }
    state.protocolRepairAttempts++;
    return true;
  }

  private String buildToolCallSignature(JsonNode toolCall) {
    String toolName = requiredJsonText(toolCall.path(FUNCTION_FIELD), NAME_FIELD);
    String arguments = requiredJsonText(toolCall.path(FUNCTION_FIELD), ARGUMENTS_FIELD);

    try {
      JsonNode parsedArguments = objectMapper.readTree(arguments);
      return toolName + ":" + canonicalize(parsedArguments);
    } catch (JsonProcessingException exception) {
      return toolName + ":" + arguments.trim();
    }
  }

  private String canonicalize(JsonNode node) {
    if (node.isObject()) {
      TreeMap<String, JsonNode> sorted = new TreeMap<>();
      node.fields().forEachRemaining(entry -> sorted.put(entry.getKey(), entry.getValue()));
      return sorted.entrySet().stream()
          .map(entry -> "\"" + entry.getKey() + "\":" + canonicalize(entry.getValue()))
          .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    }
    if (node.isArray()) {
      List<String> values = new ArrayList<>();
      node.forEach(child -> values.add(canonicalize(child)));
      return values.stream().collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
    return node.toString();
  }

  private boolean likelyRequiresTool(String userMessage) {
    String normalized = userMessage.toLowerCase();
    return normalized.contains("add")
        || normalized.contains("create")
        || normalized.contains("remove")
        || normalized.contains("delete")
        || normalized.contains("resize")
        || normalized.contains("move")
        || normalized.contains("change")
        || normalized.contains("update")
        || normalized.contains("rename")
        || normalized.contains("chart")
        || normalized.contains("widget")
        || normalized.contains("dashboard");
  }

  private void validateProperties(JsonNode args, Set<String> allowedFields) {
    args.fieldNames()
        .forEachRemaining(
            field -> {
              if (!allowedFields.contains(field)) {
                throw new IllegalArgumentException("Unknown tool argument: " + field);
              }
            });
  }

  private JsonNode requiredNode(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (value.isMissingNode() || value.isNull()) {
      throw new IllegalArgumentException("Missing required tool argument: " + field);
    }
    return value;
  }

  private String requiredJsonText(JsonNode node, String field) {
    JsonNode value = requiredNode(node, field);
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new IllegalArgumentException("Tool argument must be a non-blank string: " + field);
    }
    return value.asText();
  }

  private UUID requiredUuid(JsonNode node, String field) {
    try {
      return UUID.fromString(requiredJsonText(node, field));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Invalid UUID tool argument: " + field);
    }
  }

  private String optionalText(JsonNode node, String field) {
    if (!node.hasNonNull(field)) {
      return null;
    }
    JsonNode value = node.path(field);
    if (!value.isTextual()) {
      throw new IllegalArgumentException("Tool argument must be a string: " + field);
    }
    return value.asText();
  }

  private Integer optionalInteger(JsonNode node, String field) {
    if (!node.hasNonNull(field)) {
      return null;
    }
    JsonNode value = node.path(field);
    if (!value.isInt()) {
      throw new IllegalArgumentException("Tool argument must be an integer: " + field);
    }
    return value.intValue();
  }

  private String buildToolErrorMessage(String toolName, Exception exception) {
    String message = exception.getMessage();
    if (message == null || message.isBlank()) {
      message = exception.getClass().getSimpleName();
    }
    return MESSAGE_TOOL_ERROR_PREFIX + toolName + ": " + message;
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize AI agent payload", exception);
    }
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

  private List<Map<String, Object>> toolDefinitions() {
    List<Map<String, Object>> tools = new ArrayList<>();

    tools.add(
        tool(
            FIND_ACCOUNTS_TOOL,
            "Find the user's cloud accounts. Use query to narrow results. Never invent account IDs.",
            objectSchema(
                Map.of(
                    "query", nullableStringProperty(),
                    "limit", nullableIntegerProperty(1, 10)),
                List.of("query", "limit"))));

    tools.add(
        tool(
            FIND_RESOURCES_TOOL,
            "Find resources inside one owned cloud account. Use resourceType or query to narrow results.",
            objectSchema(
                Map.of(
                    "accountId", uuidProperty(),
                    "query", nullableStringProperty(),
                    "resourceType", nullableStringProperty(),
                    "limit", nullableIntegerProperty(1, 12)),
                List.of("accountId", "query", "resourceType", "limit"))));

    tools.add(
        tool(
            FIND_METRICS_TOOL,
            "Find metrics for one resource. The result also identifies the resource account and provider.",
            objectSchema(
                Map.of(
                    "resourceId", uuidProperty(),
                    "query", nullableStringProperty(),
                    "limit", nullableIntegerProperty(1, 16)),
                List.of("resourceId", "query", "limit"))));

    tools.add(
        tool(
            FIND_CHARGES_TOOL,
            "Find billing charge IDs available to this user. Never invent charge IDs.",
            objectSchema(
                Map.of(
                    "query", nullableStringProperty(),
                    "limit", nullableIntegerProperty(1, 16)),
                List.of("query", "limit"))));

    tools.add(
        tool(
            ADD_CHART_TOOL,
            "Add one chart widget. The backend derives provider, accountId, and metricType from resourceId. "
                + "Use discovery first unless the resourceId and metricName are already known.",
            objectSchema(
                Map.of(
                    "displayName",
                    stringProperty(),
                    "resourceId",
                    uuidProperty(),
                    "metricName",
                    stringProperty(),
                    "chartType",
                    nullableEnumProperty(List.of("line_chart", "gauge_chart")),
                    "chartColour",
                    nullableEnumProperty(
                        List.of("chart_1", "chart_2", "chart_3", "chart_4", "chart_5")),
                    "startX",
                    nullableIntegerProperty(0, 12),
                    "startY",
                    nullableIntegerProperty(0, null),
                    "width",
                    nullableIntegerProperty(1, 12),
                    "height",
                    nullableIntegerProperty(1, null)),
                List.of(
                    "displayName",
                    "resourceId",
                    "metricName",
                    "chartType",
                    "chartColour",
                    "startX",
                    "startY",
                    "width",
                    "height"))));

    tools.add(
        tool(
            ADD_KPI_TOOL,
            "Add one KPI widget. The backend generates the widget ID and chooses a free "
                + "position when layout is omitted.",
            objectSchema(
                Map.of(
                    "displayName", stringProperty(),
                    "chargeIds", arrayStringProperty(),
                    "aggregationWindowDays", nullableIntegerProperty(1, null),
                    "startX", nullableIntegerProperty(0, 12),
                    "startY", nullableIntegerProperty(0, null),
                    "width", nullableIntegerProperty(1, 12),
                    "height", nullableIntegerProperty(1, null)),
                List.of(
                    "displayName",
                    "chargeIds",
                    "aggregationWindowDays",
                    "startX",
                    "startY",
                    "width",
                    "height"))));

    tools.add(
        tool(
            UPDATE_LAYOUT_TOOL,
            "Resize or move an existing widget. Omitted fields are represented as null and are preserved. "
                + "The backend moves the widget to a free position if the requested layout collides.",
            objectSchema(
                Map.of(
                    "widgetId", uuidProperty(),
                    "startX", nullableIntegerProperty(0, 12),
                    "startY", nullableIntegerProperty(0, null),
                    "width", nullableIntegerProperty(1, 12),
                    "height", nullableIntegerProperty(1, null)),
                List.of("widgetId", "startX", "startY", "width", "height"))));

    tools.add(
        tool(
            UPDATE_CHART_TOOL,
            "Update an existing chart's configuration. Omitted fields are represented as null and are preserved; "
                + "provider/account/metricType are derived by the backend.",
            objectSchema(
                Map.of(
                    "widgetId",
                    uuidProperty(),
                    "displayName",
                    nullableStringProperty(),
                    "chartType",
                    nullableEnumProperty(List.of("line_chart", "gauge_chart")),
                    "chartColour",
                    nullableEnumProperty(
                        List.of("chart_1", "chart_2", "chart_3", "chart_4", "chart_5")),
                    "resourceId",
                    nullableUuidProperty(),
                    "metricName",
                    nullableStringProperty()),
                List.of(
                    "widgetId",
                    "displayName",
                    "chartType",
                    "chartColour",
                    "resourceId",
                    "metricName"))));

    tools.add(
        tool(
            UPDATE_KPI_TOOL,
            "Update an existing KPI's configuration. Omitted fields are represented as null and are preserved.",
            objectSchema(
                Map.of(
                    "widgetId", uuidProperty(),
                    "displayName", nullableStringProperty(),
                    "chargeIds", nullableArrayStringProperty(),
                    "aggregationWindowDays", nullableIntegerProperty(1, null)),
                List.of("widgetId", "displayName", "chargeIds", "aggregationWindowDays"))));

    tools.add(
        tool(
            DELETE_WIDGET_TOOL,
            "Delete one existing widget from the working dashboard.",
            objectSchema(Map.of("widgetId", uuidProperty()), List.of("widgetId"))));

    tools.add(
        tool(
            CLEAR_DASHBOARD_TOOL,
            "Clear all widgets from the working dashboard. Use this when the user asks for "
                + "an entirely new dashboard or to start over.",
            objectSchema(Map.of(), List.of())));

    tools.add(
        tool(
            DISCARD_DASHBOARD_TOOL,
            "Discard all uncommitted working changes and return to the last committed AI version.",
            objectSchema(Map.of(), List.of())));

    tools.add(
        tool(
            UPDATE_DASHBOARD_TOOL,
            "Update dashboard metadata. Omitted fields are represented as null and are preserved.",
            objectSchema(
                Map.of(
                    "title", nullableStringProperty(),
                    "description", nullableStringProperty(),
                    "timeFrom", nullableStringProperty(),
                    "timeTo", nullableStringProperty(),
                    "predefinedTime", nullableStringProperty()),
                List.of("title", "description", "timeFrom", "timeTo", "predefinedTime"))));

    tools.add(
        tool(
            COMMIT_TOOL,
            "Commit the current working dashboard as a new immutable AI version. "
                + "Call this exactly once after all requested changes are complete.",
            objectSchema(Map.of(), List.of())));

    return List.copyOf(tools);
  }

  private Map<String, Object> tool(
      String name, String description, Map<String, Object> parameters) {
    return Map.of(
        TYPE_FIELD,
        "function",
        "function",
        Map.of(
            NAME_FIELD,
            name,
            "description",
            description,
            "parameters",
            parameters,
            "strict",
            true));
  }

  private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
    return Map.of(
        TYPE_FIELD,
        "object",
        "properties",
        properties,
        "required",
        required,
        "additionalProperties",
        false);
  }

  private Map<String, Object> stringProperty() {
    return Map.of("type", "string");
  }

  private Map<String, Object> nullableStringProperty() {
    return Map.of("type", List.of("string", "null"));
  }

  private Map<String, Object> uuidProperty() {
    return Map.of("type", "string", "description", "UUID");
  }

  private Map<String, Object> nullableUuidProperty() {
    return Map.of("type", List.of("string", "null"), "description", "UUID or null");
  }

  private Map<String, Object> integerProperty(Integer minimum, Integer maximum) {
    Map<String, Object> property = new LinkedHashMap<>();
    property.put("type", "integer");
    if (minimum != null) {
      property.put("minimum", minimum);
    }
    if (maximum != null) {
      property.put("maximum", maximum);
    }
    return property;
  }

  private Map<String, Object> nullableIntegerProperty(Integer minimum, Integer maximum) {
    Map<String, Object> property = new LinkedHashMap<>();
    property.put("type", List.of("integer", "null"));
    if (minimum != null) {
      property.put("minimum", minimum);
    }
    if (maximum != null) {
      property.put("maximum", maximum);
    }
    return property;
  }

  private Map<String, Object> enumProperty(List<String> values) {
    return Map.of("type", "string", "enum", values);
  }

  private Map<String, Object> nullableEnumProperty(List<String> values) {
    return Map.of("type", List.of("string", "null"), "enum", values);
  }

  private Map<String, Object> arrayStringProperty() {
    return Map.of("type", "array", "items", Map.of("type", "string"));
  }

  private Map<String, Object> nullableArrayStringProperty() {
    return Map.of("type", List.of("array", "null"), "items", Map.of("type", "string"));
  }

  private record ToolCallResult(String toolCallId, String toolName, AiToolResultDto toolResult) {}

  private static final class AgentRunState {
    private final String userMessage;
    private final Map<String, ToolCallResult> completedToolCalls = new LinkedHashMap<>();
    private final Map<String, Integer> repeatedToolCalls = new LinkedHashMap<>();
    private int toolCallCount;
    private int noToolRepairAttempts;
    private int protocolRepairAttempts;
    private boolean mutationAttempted;
    private boolean commitAttempted;

    private AgentRunState(String userMessage) {
      this.userMessage = userMessage;
    }
  }
}
