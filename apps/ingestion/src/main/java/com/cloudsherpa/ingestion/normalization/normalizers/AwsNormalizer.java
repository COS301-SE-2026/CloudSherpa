package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import java.util.UUID;

public class AwsNormalizer implements Normalizer {
  public NormalizedMetric normalize(UsageRecordModel r) {
    if (r == null) {
      return null;
    }

    String metricId = UUID.randomUUID().toString();
    String resourceId = r.getResourceId();
    // account_id
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

    return new NormalizedMetric(
        metricId,
        resourceId,
        metricType,
        metricName,
        metricValue,
        unit,
        currency,
        periodStart,
        periodEnd);
  }
}
