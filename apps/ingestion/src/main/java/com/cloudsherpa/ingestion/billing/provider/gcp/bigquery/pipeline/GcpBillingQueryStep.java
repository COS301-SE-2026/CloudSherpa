package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions.QueryFailedException;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.TableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class GcpBillingQueryStep implements GcpBillingIngestionStep {
  private final Logger logger = LoggerFactory.getLogger(GcpBillingQueryStep.class);

  public void execute(GcpBillingContext context) {
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
      throw new QueryFailedException(e);
    } catch (InterruptedException e) {
      logger.error("BigQuery query failed due to interrupt: {}", e.getMessage());
      Thread.currentThread().interrupt();
      throw new QueryFailedException(e);
    }
  }
}
