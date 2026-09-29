package com.cloudsherpa.service.unit.agenticdashboard.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.AiSession;
import com.cloudsherpa.lib.repositories.AiMessageRepository;
import com.cloudsherpa.service.agenticdashboard.agent.AiAgentContext;
import com.cloudsherpa.service.agenticdashboard.agent.AiModelClient;
import com.cloudsherpa.service.agenticdashboard.agent.AiTextualToolCallParser;
import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.mcp.McpTools;
import com.cloudsherpa.service.agenticdashboard.service.AiAgentService;
import com.cloudsherpa.service.agenticdashboard.service.AiDashboardVersionService;
import com.cloudsherpa.service.agenticdashboard.service.AiSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiAgentServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private static final UUID SESSION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final UUID DASHBOARD_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  private static final UUID WIDGET_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  private static final String DELETE_WIDGET_TOOL = "delete_widget";
  private static final String COMMIT_TOOL = "commit_dashboard_changes";

  @Mock private AiSessionService aiSessionService;
  @Mock private AiDashboardVersionService versionService;
  @Mock private AiMessageRepository messageRepository;
  @Mock private AiModelClient modelClient;
  @Mock private AiTextualToolCallParser textualToolCallParser;
  @Mock private McpTools mcpTools;

  @Mock private AiSession session;
  @Mock private DashboardPlanDto dashboard;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private AiAgentService service;

  @BeforeEach
  void setUp() {
    service =
        new AiAgentService(
            aiSessionService,
            versionService,
            messageRepository,
            modelClient,
            textualToolCallParser,
            mcpTools,
            objectMapper,
            10,
            20);

    when(aiSessionService.getSession(USER_ID, SESSION_ID)).thenReturn(session);
    when(session.getCurrentVersionId()).thenReturn(null);

    when(versionService.getWorkingDashboard(USER_ID, SESSION_ID)).thenReturn(dashboard);
    when(versionService.hasWorkingDraft(USER_ID, SESSION_ID)).thenReturn(false);

    when(dashboard.title()).thenReturn("Test Dashboard");
    when(dashboard.description()).thenReturn(null);
    when(dashboard.timeFrom()).thenReturn(null);
    when(dashboard.timeTo()).thenReturn(null);
    when(dashboard.predefinedTime()).thenReturn(null);
    when(dashboard.widgets()).thenReturn(List.of());
  }

  @Test
  void shouldExecuteMutationThenCommit() {
    JsonNode deleteCall =
        toolCall(
            "call-1",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode commitCall = toolCall("call-2", COMMIT_TOOL, "{}");

    when(modelClient.complete(anyList(), anyList(), anyBoolean()))
        .thenReturn(assistantWithTool(deleteCall))
        .thenReturn(assistantWithTool(commitCall));

    AiDashboardPlanResponseDto result =
        service.generateDashboardPlan(
            USER_ID, SESSION_ID, DASHBOARD_ID, "Remove the old widget and save the changes.");

    assertNotNull(result);
    assertTrue(result.stageAttempted());
    assertTrue(result.stageSucceeded());

    verify(versionService).createInitialVersion(USER_ID, SESSION_ID, DASHBOARD_ID);
    verify(mcpTools).deleteWidget(any(AiAgentContext.class), eq(WIDGET_ID));
    verify(mcpTools).commitDashboardChanges(any(AiAgentContext.class));
    verify(modelClient, times(2)).complete(anyList(), anyList(), anyBoolean()); // 2 calls executed
  }

  @Test
  void shouldNotExecuteTheSameSuccessfulToolCallTwice() {
    JsonNode firstDelete =
        toolCall(
            "call-1",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode duplicateDelete =
        toolCall(
            "call-2",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode commitCall = toolCall("call-3", COMMIT_TOOL, "{}");

    when(modelClient.complete(anyList(), anyList(), anyBoolean()))
        .thenReturn(assistantWithTool(firstDelete))
        .thenReturn(assistantWithTool(duplicateDelete))
        .thenReturn(assistantWithTool(commitCall));

    AiDashboardPlanResponseDto result =
        service.generateDashboardPlan(
            USER_ID, SESSION_ID, DASHBOARD_ID, "Delete that widget and commit the result.");

    assertNotNull(result);
    assertTrue(result.stageSucceeded());

    verify(mcpTools, times(1)).deleteWidget(any(AiAgentContext.class), eq(WIDGET_ID));

    verify(mcpTools, times(1)).commitDashboardChanges(any(AiAgentContext.class));

    verify(modelClient, times(3)).complete(anyList(), anyList(), anyBoolean());
  }

  @Test
  void shouldRepairWhenModelReturnsMultipleToolCalls() {
    JsonNode deleteOne =
        toolCall(
            "call-1",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode deleteTwo =
        toolCall(
            "call-2",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode validDelete =
        toolCall(
            "call-3",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode commitCall = toolCall("call-4", COMMIT_TOOL, "{}");

    when(modelClient.complete(anyList(), anyList(), anyBoolean()))
        .thenReturn(assistantWithTools(deleteOne, deleteTwo))
        .thenReturn(assistantWithTool(validDelete))
        .thenReturn(assistantWithTool(commitCall));

    AiDashboardPlanResponseDto result =
        service.generateDashboardPlan(USER_ID, SESSION_ID, DASHBOARD_ID, "Delete the widget.");

    assertNotNull(result);
    assertTrue(result.stageSucceeded());

    // The first response contains two calls and therefore neither gets executed as
    // we require one tool call per message
    // Repaired response is the only delete action that reaches the MCP layer
    verify(mcpTools, times(1)).deleteWidget(any(AiAgentContext.class), eq(WIDGET_ID));

    verify(mcpTools, times(1)).commitDashboardChanges(any(AiAgentContext.class));

    verify(modelClient, times(3)).complete(anyList(), anyList(), anyBoolean());
  }

  @Test
  void shouldPassToolFailureBackToModelInsteadOfThrowing() {
    JsonNode deleteCall =
        toolCall(
            "call-1",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode commitCall = toolCall("call-2", COMMIT_TOOL, "{}");

    doThrow(new IllegalStateException("Widget no longer exists"))
        .when(mcpTools)
        .deleteWidget(any(AiAgentContext.class), eq(WIDGET_ID));

    when(modelClient.complete(anyList(), anyList(), anyBoolean()))
        .thenReturn(assistantWithTool(deleteCall))
        .thenReturn(assistantWithTool(commitCall));

    AiDashboardPlanResponseDto result =
        service.generateDashboardPlan(USER_ID, SESSION_ID, DASHBOARD_ID, "Delete the widget.");

    assertNotNull(result);
    assertTrue(result.stageSucceeded());

    verify(mcpTools).deleteWidget(any(AiAgentContext.class), eq(WIDGET_ID));
    verify(mcpTools).commitDashboardChanges(any(AiAgentContext.class));
    verify(modelClient, times(2)).complete(anyList(), anyList(), anyBoolean());
  }

  @Test
  void shouldAutoCommitWorkingDraftWhenModelFails() {
    when(versionService.hasUncommittedChanges(USER_ID, SESSION_ID)).thenReturn(true);

    when(modelClient.complete(anyList(), anyList(), anyBoolean()))
        .thenThrow(new IllegalStateException("LLM unavailable"));

    AiDashboardPlanResponseDto result =
        service.generateDashboardPlan(USER_ID, SESSION_ID, DASHBOARD_ID, "Change the dashboard.");

    assertNotNull(result);
    assertTrue(result.stageAttempted());
    assertTrue(result.stageSucceeded());

    verify(versionService).commitWorkingDashboard(USER_ID, SESSION_ID);
    verify(modelClient, times(1)).complete(anyList(), anyList(), anyBoolean());
  }

  @Test
  void shouldSupportLegacyTextualToolCallPath() {
    JsonNode textualDelete =
        toolCall(
            "textual-call-1",
            DELETE_WIDGET_TOOL,
            """
            {"widgetId":"11111111-2222-3333-4444-555555555555"}
            """);

    JsonNode commitCall = toolCall("call-2", COMMIT_TOOL, "{}");

    when(modelClient.complete(anyList(), anyList(), anyBoolean()))
        .thenReturn(assistantWithContent("some textual tool call"))
        .thenReturn(assistantWithTool(commitCall));

    when(textualToolCallParser.parse("some textual tool call")).thenReturn(textualDelete);

    AiDashboardPlanResponseDto result =
        service.generateDashboardPlan(USER_ID, SESSION_ID, DASHBOARD_ID, "Delete the widget.");

    assertNotNull(result);
    assertTrue(result.stageSucceeded());

    verify(textualToolCallParser).parse("some textual tool call");
    verify(mcpTools).deleteWidget(any(AiAgentContext.class), eq(WIDGET_ID));
    verify(mcpTools).commitDashboardChanges(any(AiAgentContext.class));
    verify(modelClient, times(2)).complete(anyList(), anyList(), anyBoolean());
  }

  private JsonNode assistantWithTool(JsonNode toolCall) {
    return assistantWithTools(toolCall);
  }

  private JsonNode assistantWithTools(JsonNode... toolCalls) {
    ObjectNode root = objectMapper.createObjectNode();
    ArrayNode choices = root.putArray("choices");

    ObjectNode message = choices.addObject().putObject("message");

    message.put("role", "assistant");

    ArrayNode calls = message.putArray("tool_calls");
    for (JsonNode toolCall : toolCalls) {
      calls.add(toolCall);
    }

    return root;
  }

  private JsonNode assistantWithContent(String content) {
    ObjectNode root = objectMapper.createObjectNode();
    ArrayNode choices = root.putArray("choices");

    ObjectNode message = choices.addObject().putObject("message");

    message.put("role", "assistant");
    message.put("content", content);

    return root;
  }

  private ObjectNode toolCall(String id, String name, String arguments) {
    ObjectNode call = objectMapper.createObjectNode();

    call.put("id", id);
    call.put("type", "function");

    ObjectNode function = call.putObject("function");
    function.put("name", name);
    function.put("arguments", arguments);

    return call;
  }
}
