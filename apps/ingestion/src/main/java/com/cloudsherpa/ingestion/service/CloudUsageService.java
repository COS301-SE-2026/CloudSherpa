package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.connector.AccountScope;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
        List<UsageRecordModel> usageRecords = usageCapable.fetchUsage(request);
        usageResults.addAll(usageRecords);
        normalizeAndPersistUsage(usageRecords);
      }

      if (request.isIncludeBilling() && connector instanceof BillingCapable billingCapable) {
        billingResults.addAll(billingCapable.fetchBilling(scope, request));
      }
    }

    return new IngestionResult(usageResults, billingResults);
  }

  public IngestionResult ingestMock(IngestionRequestEvent request) {
    List<UsageRecordModel> usageResults = new ArrayList<>();
    List<BillingRecordModel> billingResults = new ArrayList<>();

    for (AccountScope scope : request.getScopes()) {
      if (request.isIncludeUsage()) {
        List<UsageRecordModel> usageRecords = buildMockUsage(scope);
        usageResults.addAll(usageRecords);
        normalizeAndPersistUsage(usageRecords);
      }
    }

    return new IngestionResult(usageResults, billingResults);
  }

  @Autowired
  private SherpaDbPersistenceService sherpaDbPersistenceService;

  private final AwsNormalizer normalizer = new AwsNormalizer();

  private void normalizeAndPersistUsage(List<UsageRecordModel> usageRecords) {
    if (usageRecords == null || usageRecords.isEmpty()) {
      return;
    }

    UUID environmentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // still mock for now

    for (UsageRecordModel record : usageRecords) {
      NormalizedMetric normalized = normalizer.normalize(record);

      if (normalized != null) {
        writeToSherpaDb(environmentId, normalized);
      }
    }
  }

  private List<UsageRecordModel> buildMockUsage(AccountScope scope) {
    List<UsageRecordModel> results = new ArrayList<>();

    String provider = "AWS";
    if (scope.getProvider() != null) {
      provider = scope.getProvider();
    }
    String accountId = scope.getAccountId();

    String[] timestamps = {
        "2026-05-02T18:17:00+02:00",
        "2026-05-02T18:12:00+02:00",
        "2026-05-02T18:07:00+02:00",
        "2026-05-02T18:02:00+02:00",
        "2026-05-02T17:57:00+02:00",
        "2026-05-02T17:52:00+02:00",
        "2026-05-02T17:47:00+02:00"
    };

    double[] averages = {
        1.9488974910916348,
        1.8703353383091854,
        2.524456273466404,
        1.9650714832950267,
        2.436655917725189,
        13.769105242611008,
        4.416415999768938
    };

    for (int i = 0; i < timestamps.length; i++) {
      UsageRecordModel record = new UsageRecordModel();
      record.setProvider(provider);
      record.setAccountId(accountId);
      record.setServiceName("EC2");
      record.setMetricName("CPUUtilization");
      record.setResourceId("mock-ec2-" + (i + 1));
      record.setValue(averages[i]);
      record.setUnit("Percent");
      record.setTimestamp(OffsetDateTime.parse(timestamps[i]).toInstant());
      results.add(record);
    }

    return results;
  }

  private void writeToSherpaDb(UUID environmentId, NormalizedMetric metric) {
    try {
      sherpaDbPersistenceService.recordMetric(environmentId, metric);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
