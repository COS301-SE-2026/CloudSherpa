package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GcpNormalizer implements Normalizer {
  private final ResourceRepository resourceRepository;

  public GcpNormalizer(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  public NormalizedMetric normalize(UsageRecordModel r) {
    if (r == null) {
      return null;
    }

    // Took from awsNormalizer
    // My understanding: looks if there is already a resource in the database for this usage record
    UUID resourceTableIdent =
        resourceRepository
            .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
                UUID.fromString(r.getAccountId()),
                r.getServiceName(),
                r.getResourceId(),
                r.getRegion())
            .map(Resource::getId)
            .orElse(null);
    String metricId = UUID.randomUUID().toString();
    String resourceId = resourceTableIdent != null ? resourceTableIdent.toString() : null;

    // Source: gcpDataset.csv, accountId is null so I use projectId as specified in UsageRecordModel
    String accountId = r.getAccountId();
    if (accountId == null || accountId.trim().isEmpty()) {
      accountId = r.getProjectId();
    }

    String metricType = "usage";
    String metricName = "unknown";

    if (r.getMetricName() != null) {
      metricName = r.getMetricName();
    }

    String mnLower = metricName.toLowerCase();

    if (mnLower.contains("cost") || mnLower.contains("charge") || mnLower.contains("billing")) {
      metricType = "cost";
    } else if (mnLower.contains("latency")
        || mnLower.contains("duration")
        || mnLower.contains("error")
        || mnLower.contains("throttle")
        || mnLower.contains("utilization")) {
      metricType = "performance";
    }

    double metricValue = r.getValue();
    String unit = normalizeGcpUnit(r.getUnit());
    String currency = null;

    long periodStart = 0;
    if (r.getPeriodStart() != null) {
      periodStart = r.getPeriodStart().toEpochMilli();
    } else if (r.getTimestamp() != null) {
      periodStart = r.getTimestamp().toEpochMilli();
    }

    long periodEnd = 0;
    if (r.getPeriodEnd() != null) {
      periodEnd = r.getPeriodEnd().toEpochMilli();
    } else if (r.getTimestamp() != null) {
      periodEnd = r.getTimestamp().toEpochMilli();
    }

    return new NormalizedMetric.Builder()
        .metricId(metricId)
        .resourceId(resourceId)
        .accountId(accountId)
        .metricType(metricType)
        .metricName(metricName)
        .metricValue(metricValue)
        .unit(unit)
        .currency(currency)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .build();
  }

  private String normalizeGcpUnit(String gcpUnit) {
    if (gcpUnit == null) {
      return "unknown";
    }

    switch (gcpUnit.trim()) {
      case "By":
        return "bytes";
      case "s":
        return "seconds";
      case "Percent":
        return "percent";
      case "Count":
        return "count";
      case "1":
        return "count";
      default:
        return gcpUnit.toLowerCase();
    }
  }
}
