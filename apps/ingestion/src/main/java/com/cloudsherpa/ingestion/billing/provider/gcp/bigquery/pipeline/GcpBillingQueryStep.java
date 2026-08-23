package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions.QueryFailedException;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.TableResult;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class GcpBillingQueryStep implements GcpBillingIngestionStep {
  private final Logger logger = LoggerFactory.getLogger(GcpBillingQueryStep.class);
  private final BillingExportService exportService;

  public GcpBillingQueryStep(BillingExportService exportService) {
    this.exportService = exportService;
  }

  public void execute(GcpBillingContext context) {
    // Assumption: for a GCP billing export, the export execution is marked as processing from the
    // time the
    // query is made to the time all records have been written into the database
    exportService.transitionExportStatus(
        context.getBillingExport(), ExecutionStatusEnum.processing);
    context.setTableResult(makeQuery(context));
  }

  private QueryJobConfiguration getQuery(GcpBillingContext context) {
    String query =
        """
        SELECT
            project.id AS project_id,
            service.id AS service_id,
            service.description AS service_description,
            sku.id AS sku_id,
            sku.description AS sku_description,
            resource.global_name AS resource_global_name,
            resource.name AS resource_name,
            cost_type,
            cost,
            currency,
            usage_start_time,
            usage_end_time,
            export_time,
            ARRAY(
                SELECT AS STRUCT
                    credit.amount
                FROM UNNEST(credits) AS credit
            ) AS credits
        FROM `%s`
        WHERE TIMESTAMP_TRUNC(_PARTITIONTIME, DAY) > @window_start
        """
            .formatted(context.getFullyQualifiedExportTableIdentifier());

    return QueryJobConfiguration.newBuilder(query)
        .addNamedParameter(
            "window_start",
            QueryParameterValue.timestamp(context.getQueryFrom().toEpochMilli() * 1000))
        .build();
  }

  private TableResult makeQuery(GcpBillingContext context) {
    try {
      return context.getBigQueryClient().query(getQuery(context));
    } catch (BigQueryException e) {
      logger.error("BigQuery query failed with reasons {}", e.getMessage());
      updateExportExecutionQueryFailure(context);
      throw new QueryFailedException(e);
    } catch (InterruptedException e) {
      logger.error("BigQuery query failed due to interrupt: {}", e.getMessage());
      updateExportExecutionQueryFailure(context);
      Thread.currentThread().interrupt();
      throw new QueryFailedException(e);
    }
  }

  private void updateExportExecutionQueryFailure(GcpBillingContext context) {
    BillingExport billingExport = context.getBillingExport();
    billingExport.setExecutionStatus(ExecutionStatusEnum.failed);
    billingExport.setErrorMessage("BigQuery query failed");
    billingExport.setCompletedAt(OffsetDateTime.now(ZoneId.of("UTC")));
    exportService.updateDbExport(billingExport);
  }
}
