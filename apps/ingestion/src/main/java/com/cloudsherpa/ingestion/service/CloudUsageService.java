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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

/** Intermediary between the CloudUsageController and the ingestion pipeline. */
@Service
public class CloudUsageService {
  private final CloudConnectorFactory factory;
  private final SherpaDbPersistenceService sherpaDbPersistenceService;
  private final AwsNormalizer normalizer = new AwsNormalizer();

  Logger logger = Logger.getLogger(getClass().getName());

  public CloudUsageService(
      CloudConnectorFactory factory, SherpaDbPersistenceService sherpaDbPersistenceService) {
    this.factory = factory;
    this.sherpaDbPersistenceService = sherpaDbPersistenceService;
  }

  public IngestionResult ingest(IngestionRequestEvent request) {

    List<UsageRecordModel> usageResults = new ArrayList<>();
    List<BillingRecordModel> billingResults = new ArrayList<>();
    UUID userId = request.getUserId();

    for (AccountScope scope : request.getScopes()) {

      CloudConnector connector = factory.getConnector(scope.getProvider());

      if (request.isIncludeUsage() && connector instanceof UsageCapable usageCapable) {
        List<UsageRecordModel> usageRecords = usageCapable.fetchUsage(scope, request);
        usageResults.addAll(usageRecords);
        normalizeAndPersistUsage(usageRecords, userId);
      }

      if (request.isIncludeBilling() && connector instanceof BillingCapable billingCapable) {
        billingResults.addAll(billingCapable.fetchBilling(scope, request));
      }
    }

    return new IngestionResult(usageResults, billingResults);
  }

  public IngestionResult ingestMockWithNoise(IngestionRequestEvent request) {

    List<UsageRecordModel> usageResults = new ArrayList<>();
    List<BillingRecordModel> billingResults = new ArrayList<>();
    UUID userId = request.getUserId();

    for (AccountScope scope : request.getScopes()) {

      CloudConnector connector = factory.getConnector(scope.getProvider());

      if (request.isIncludeUsage() && connector instanceof UsageCapable usageCapable) {
        List<UsageRecordModel> usageRecords = usageCapable.fetchMockUsage(scope, request);
        usageResults.addAll(usageRecords);
        normalizeAndPersistUsage(usageRecords, userId);
      }

      if (request.isIncludeBilling() && connector instanceof BillingCapable billingCapable) {
        billingResults.addAll(billingCapable.fetchMockBilling(scope, request));
      }
    }

    return new IngestionResult(usageResults, billingResults);
  }

  public IngestionResult ingestMock(IngestionRequestEvent request) {
    List<UsageRecordModel> usageResults = new ArrayList<>();
    List<BillingRecordModel> billingResults = new ArrayList<>();
    UUID userId = request.getUserId();

    for (AccountScope scope : request.getScopes()) {
      if (request.isIncludeUsage()) {
        List<UsageRecordModel> usageRecords = buildMockUsage(scope, request);
        usageResults.addAll(usageRecords);
        normalizeAndPersistUsage(usageRecords, userId);
      }
    }

    return new IngestionResult(usageResults, billingResults);
  }

  private void normalizeAndPersistUsage(List<UsageRecordModel> usageRecords, UUID userId) {
    if (usageRecords == null || usageRecords.isEmpty()) {
      return;
    }

    for (UsageRecordModel r : usageRecords) {
      NormalizedMetric normalized = normalizer.normalize(r);
      if (normalized != null) {
        try {
          writeToSherpaDb(normalized, r, userId);
        } catch (RuntimeException ex) {
          logger.warning("Failed to persist normalized metric: " + ex.getMessage());
        }
      }
    }
  }

  private List<UsageRecordModel> buildMockUsage(AccountScope scope, IngestionRequestEvent request) {
    List<UsageRecordModel> results = new ArrayList<>();

    String provider = "AWS";
    if (scope.getProvider() != null) {
      provider = scope.getProvider();
    }
    String accountId = scope.getAccountId();

    OffsetDateTime to;
    if (request.getTo() != null) {
      to = request.getTo().atOffset(ZoneOffset.UTC);
    } else {
      to = OffsetDateTime.now(ZoneOffset.UTC);
    }

    OffsetDateTime from;
    if (request.getFrom() != null) {
      from = request.getFrom().atOffset(ZoneOffset.UTC);
    } else {
      from = to.minusMinutes(30);
    }

    if (from.isAfter(to)) {
      from = to.minusMinutes(30);
    }

    String[] timestamps = new String[7];
    long totalSeconds = Math.max(1, to.toEpochSecond() - from.toEpochSecond());
    long stepSeconds = Math.max(1, totalSeconds / (timestamps.length - 1));

    for (int i = 0; i < timestamps.length; i++) {
      timestamps[i] = from.plusSeconds(stepSeconds * i).toString();
    }

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
      UsageRecordModel usageRecord = new UsageRecordModel();
      usageRecord.setProvider(provider);
      usageRecord.setAccountId(accountId);
      usageRecord.setServiceName("EC2");
      usageRecord.setResourceType("ec2_instance");
      usageRecord.setMetricName("CPUUtilization");
      usageRecord.setResourceId("mock-ec2-" + (i + 1));
      usageRecord.setValue(averages[i]);
      usageRecord.setUnit("Percent");
      usageRecord.setTimestamp(OffsetDateTime.parse(timestamps[i]).toInstant());
      results.add(usageRecord);
    }

    return results;
  }

  private void writeToSherpaDb(NormalizedMetric metric, UsageRecordModel r, UUID userId) {
    sherpaDbPersistenceService.recordMetric(metric, r, userId);
  }
}
