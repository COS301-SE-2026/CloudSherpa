package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.ExportExecutionHistoryStep;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportExecutionHistoryStepTest {
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final String EXISTING_RUN_ID = "existing-run";
  private static final String NEW_RUN_ID = "new-run";
  private static final UUID EXISTING_EXECUTION_ID = executionId(EXISTING_RUN_ID);
  private static final UUID NEW_EXECUTION_ID = executionId(NEW_RUN_ID);

  @Mock BillingExportExecutionRepository executionRepository;
  @Mock BillingExportService exportService;

  @InjectMocks ExportExecutionHistoryStep step;

  @Test
  void shouldFilterOutExistingExportExecutions() {
    AzureBillingContext context = getPopulatedContext();
    when(executionRepository.findByConfigId(CONFIG_ID)).thenReturn(getExecutions());

    step.execute(context);

    assertEquals(1, context.getManifests().size());
    assertNull(context.getManifests().get(EXISTING_EXECUTION_ID));
  }

  @Test
  void shouldInitializeNewExportExecutions() {
    AzureBillingContext context = getPopulatedContext();
    when(executionRepository.findByConfigId(CONFIG_ID)).thenReturn(getExecutions());

    step.execute(context);

    verify(exportService).initializeExportExecution(NEW_EXECUTION_ID, CONFIG_ID);
  }

  private AzureBillingContext getPopulatedContext() {

    AzureBillingContext context = new AzureBillingContext(USER_ID, CONFIG_ID);
    context.setExportConfig(
        new AzureBillingExportConfig(
            CONFIG_ID, "teststorageaccount", "billing-exports", "exports/daily", "test-export"));
    context.setManifests(
        new HashMap<>(
            Map.of(
                EXISTING_EXECUTION_ID, manifest(EXISTING_RUN_ID),
                NEW_EXECUTION_ID, manifest(NEW_RUN_ID))));
    context.setExecutions(new HashMap<>());
    return context;
  }

  private List<BillingExportExecution> getExecutions() {
    return List.of(
        new BillingExportExecution(
            EXISTING_EXECUTION_ID,
            CONFIG_ID,
            ExecutionStatusEnum.completed,
            2,
            OffsetDateTime.parse("2026-09-01T01:00:00Z"),
            OffsetDateTime.parse("2026-09-01T01:05:00Z"),
            null));
  }

  private AzureManifest manifest(String runId) {
    return new AzureManifest(
        100,
        1,
        2,
        new AzureManifest.RunInfo(
            Instant.parse("2026-09-01T00:30:00Z"),
            runId,
            Instant.parse("2026-08-01T00:00:00Z"),
            Instant.parse("2026-09-01T00:00:00Z")),
        new AzureManifest.DeliveryConfig("Csv"),
        List.of(
            new AzureManifest.Partition(
                "exports/daily/test-export/" + runId + "/part0.csv", 100, 2)));
  }

  private static UUID executionId(String runId) {
    return UUID.nameUUIDFromBytes((CONFIG_ID + ":" + runId).getBytes(StandardCharsets.UTF_8));
  }
}
