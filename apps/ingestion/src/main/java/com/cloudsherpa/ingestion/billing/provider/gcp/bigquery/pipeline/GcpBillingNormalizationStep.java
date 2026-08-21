package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.GcpBigQueryNormalizationService;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class GcpBillingNormalizationStep implements GcpBillingIngestionStep {

  private final BillingExportService billingExportService;
  private final Logger logger = // NOSONAR will use logger in future
      LoggerFactory.getLogger(GcpBillingNormalizationStep.class);
  private final GcpBigQueryNormalizationService gcpBigQueryNormalizationService;

  public GcpBillingNormalizationStep(
      GcpBigQueryNormalizationService gcpBigQueryNormalizationService,
      BillingExportService billingExportService) {
    this.gcpBigQueryNormalizationService = gcpBigQueryNormalizationService;
    this.billingExportService = billingExportService;
  }

  public void execute(GcpBillingContext context) {
    gcpBigQueryNormalizationService.normalize(context);
    updateExportExecutionIngestionSuccess(context);
  }

  private void updateExportExecutionIngestionSuccess(GcpBillingContext context) {
    BillingExport billingExport = context.getBillingExport();

    billingExport.setCompletedAt(OffsetDateTime.now(ZoneId.of("UTC")));
    billingExport.setRowsProcessed(Math.toIntExact(context.getTableResult().getTotalRows()));
    billingExport.setExecutionStatus(ExecutionStatusEnum.completed);
    billingExportService.updateDbExport(billingExport);
  }
}
