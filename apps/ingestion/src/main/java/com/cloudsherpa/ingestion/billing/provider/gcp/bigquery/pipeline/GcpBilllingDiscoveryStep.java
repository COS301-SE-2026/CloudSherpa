package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions.DatasetNotFoundException;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions.TableNotFoundException;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Inspiration taken from GCP docs
// https://docs.cloud.google.com/bigquery/docs/samples/bigquery-dataset-exists
// https://docs.cloud.google.com/bigquery/docs/samples/bigquery-table-exists

@Component
@Order(2)
public class GcpBilllingDiscoveryStep implements GcpBillingIngestionStep {

  private final Logger logger = LoggerFactory.getLogger(GcpBilllingDiscoveryStep.class);

  public void execute(GcpBillingContext context) {
    // Ensures dataset exists, will throw DatasetNotFoundException if it does not exist
    datasetExists(context);
    String tableId = constructTableId(context);
    tableExists(context, tableId);
    context.setBillingExportTableIdentifier(tableId);
    context.setFullyQualifiedExportTableIdentifier(constructFullyQualifiedTableId(context));
  }

  private void datasetExists(GcpBillingContext context) {
    try {
      context
          .getBigQueryClient()
          .getDataset(
              DatasetId.of(
                  context.getBillingConfig().projectId(), context.getBillingConfig().datasetId()));
    } catch (BigQueryException e) {
      logger.error("Dataset {} not found", context.getBillingConfig().datasetId());
      throw new DatasetNotFoundException(context.getBillingConfig().datasetId(), e);
    }
  }

  private void tableExists(GcpBillingContext context, String tableId) {
    try {
      Table table =
          context
              .getBigQueryClient()
              .getTable(
                  TableId.of(
                      context.getBillingConfig().projectId(),
                      context.getBillingConfig().datasetId(),
                      tableId));
      if (table == null) {
        throw new TableNotFoundException(tableId, null);
      }
    } catch (BigQueryException e) {
      logger.error(
          "Table {} not found on dataset {}", tableId, context.getBillingConfig().datasetId());
      throw new TableNotFoundException(tableId, e);
    }
  }

  private String constructTableId(GcpBillingContext context) {
    StringBuilder tableId = new StringBuilder();

    tableId
        .append("gcp_billing_export_resource_v1_")
        .append(context.getBillingConfig().billingAccountId().replace("-", "_"));

    return tableId.toString();
  }

  private String constructFullyQualifiedTableId(GcpBillingContext context) {
    StringBuilder fullyQualifiedTableId = new StringBuilder();

    GcpBillingConfig gcpBillingConfig = context.getBillingConfig();

    fullyQualifiedTableId
        .append(gcpBillingConfig.projectId())
        .append(".")
        .append(gcpBillingConfig.datasetId())
        .append(".")
        .append(constructTableId(context));

    return fullyQualifiedTableId.toString();
  }
}
