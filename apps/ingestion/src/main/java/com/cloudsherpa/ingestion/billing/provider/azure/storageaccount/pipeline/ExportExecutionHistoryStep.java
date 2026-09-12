package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class ExportExecutionHistoryStep
    implements BillingIngestionPipelineStep<AzureBillingContext> {
  private final BillingExportExecutionRepository exportExecutionRepository;
  private final BillingExportService billingExportService;

  public ExportExecutionHistoryStep(
      BillingExportExecutionRepository exportExecutionRepository,
      BillingExportService billingExportService) {
    this.exportExecutionRepository = exportExecutionRepository;
    this.billingExportService = billingExportService;
  }

  public void execute(AzureBillingContext context) {
    List<BillingExportExecution> executionHistory =
        exportExecutionRepository.findByConfigId(context.getConfigId());

    List<UUID> executionHistoryIds =
        executionHistory.stream().map(BillingExportExecution::getId).toList();

    Map<UUID, AzureManifest> filteredManifests = new HashMap<>();
    Map<UUID, BillingExportExecution> executions = new HashMap<>();

    context
        .getManifests()
        .forEach(
            (key, value) -> {
              if (!executionHistoryIds.contains(key)) {
                filteredManifests.put(key, value);
                executions.put(
                    key,
                    billingExportService.initializeExportExecution(key, context.getConfigId()));
              }
            });

    context.setManifests(filteredManifests);
    context.setExecutions(executions);
  }
}
