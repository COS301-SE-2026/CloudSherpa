package com.cloudsherpa.ingestion.billing;

import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingExportService {
  private final BillingExportExecutionRepository billingExportExecutionRepository;

  public BillingExportService(BillingExportExecutionRepository billingExportExecutionRepository) {
    this.billingExportExecutionRepository = billingExportExecutionRepository;
  }

  @Transactional
  public BillingExport initializeExport(String exportId, String configId, List<String> dataFiles) {
    BillingExport newExport = new BillingExport(exportId, configId, dataFiles);
    billingExportExecutionRepository.save(
        new BillingExportExecution(
            UUID.fromString(exportId), UUID.fromString(configId), ExecutionStatusEnum.pending));
    return newExport;
  }

  @Transactional
  public void transitionExportStatus(BillingExport export, ExecutionStatusEnum status) {
    // update in DB
    BillingExportExecution execution =
        billingExportExecutionRepository.findById(export.getUuidExportId()).orElseThrow();
    execution.setStatus(status);
    billingExportExecutionRepository.save(execution);

    // update object
    export.setExecutionStatus(status);
  }

  @Transactional
  public void updateDbExport(BillingExport export) {
    BillingExportExecution execution =
        billingExportExecutionRepository.findById(export.getUuidExportId()).orElseThrow();

    execution.setRowsProcessed(export.getRowsProcessed());
    execution.setStartedAt(export.getStartedAt());
    execution.setCompletedAt(export.getCompletedAt());
    execution.setStatus(export.getExecutionStatus());
    execution.setErrorMessage(export.getErrorMessage());

    billingExportExecutionRepository.save(execution);
  }
}
