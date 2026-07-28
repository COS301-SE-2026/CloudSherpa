package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AwsNormalizer implements Normalizer {
  private final ResourceRepository resourceRepository;

  public AwsNormalizer(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  public NormalizedMetric normalize(UsageRecordModel r) {
    if (r == null) {
      return null;
    }

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
    String accountId = r.getAccountId();
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
        || mnLower.contains("throttle")) {
      metricType = "performance";
    }

    double metricValue = r.getValue();
    String unit = r.getUnit();

    String currency = null;

    if (metricType.equals("cost")) {
      currency = "ZAR";
    }

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
}
