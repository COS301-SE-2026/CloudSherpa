package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions.DatasetNotFoundException;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.DatasetId;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Inspiration taken from GCP docs
// https://docs.cloud.google.com/bigquery/docs/samples/bigquery-dataset-exists
// https://docs.cloud.google.com/bigquery/docs/samples/bigquery-table-exists

@Component
@Order(2)
public class GcpBilllingDiscoveryStep implements GcpBillingIngestionStep {
  public void execute(GcpBillingContext context) {
    // Ensures dataset exists, will throw DatasetNotFoundException if it does not exist
    datasetExists(context);
  }

  private void datasetExists(GcpBillingContext context) {
    try {
      context.getBigQueryClient().getDataset(DatasetId.of(context.getBillingConfig().datasetId()));
    } catch (BigQueryException e) {
      throw new DatasetNotFoundException(context.getBillingConfig().datasetId());
    }
  }
}
