package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.AwsConnector;
import com.cloudsherpa.ingestion.connector.BillingCapable;
import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.connector.UsageCapable;
import com.cloudsherpa.ingestion.models.BillingRecordModel;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.AwsNormalizer;
import com.cloudsherpa.ingestion.normalization.persistence.service.SherpaDbPersistenceService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Intermediary between the CloudUsageController and the ingestion pipeline. */
@Service
public class CloudUsageService {
  private final CloudConnectorFactory factory;

  public CloudUsageService(CloudConnectorFactory factory) {
    this.factory = factory;
  }

  public IngestionResult ingest(IngestionRequestEvent request) {

    List<UsageRecordModel> usageResults = new ArrayList<>();
    List<BillingRecordModel> billingResults = new ArrayList<>();

    for (AccountScope scope : request.getScopes()) {

      CloudConnector connector = factory.getConnector(scope.getProvider());

      if (request.isIncludeUsage() && connector instanceof UsageCapable usageCapable) {
        usageResults.addAll(usageCapable.fetchUsage(scope, request));
      }

      if (request.isIncludeBilling() && connector instanceof BillingCapable billingCapable) {
        billingResults.addAll(billingCapable.fetchBilling(scope, request));
      }
    }

    return new IngestionResult(usageResults, billingResults);
  }

  @Autowired private AwsConnector awsConnector;

  @Autowired private SherpaDbPersistenceService sherpaDbPersistenceService;

  private final AwsNormalizer normalizer = new AwsNormalizer();

  public void ingestAndProcessMetrics() {
    // Get the raw string data from the connector
    List<Map<String, String>> rawMetrics = awsConnector.fetchRawData();

    // Mock environment ID (in production, this would come from user config)
    UUID environmentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // Loop through every piece of raw data
    for (Map<String, String> rawMetricRow : rawMetrics) {
      // Normalize the raw string into a clean Java object
      NormalizedMetric normalized = normalizer.normalize(rawMetricRow);

      if (normalized != null) {
        // Write the clean object directly into SherpaDB
        writeToSherpaDb(environmentId, normalized);
      }
    }
  }

  private void writeToSherpaDb(UUID environmentId, NormalizedMetric metric) {
    try {
      sherpaDbPersistenceService.recordMetric(environmentId, metric);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  // Temporary method used to test the ingestion and normalization flow.
  public NormalizedMetric sendMockEvent() {
    long now = System.currentTimeMillis();
    NormalizedMetric metric =
        new NormalizedMetric(
            "mock-metric-1",
            "AWS",
            now,
            now,
            "mock-resource-1",
            "EC2",
            "Compute",
            42.0,
            "Hours",
            12.75,
            "USD",
            "OnDemand");
    return metric;
  }
}
