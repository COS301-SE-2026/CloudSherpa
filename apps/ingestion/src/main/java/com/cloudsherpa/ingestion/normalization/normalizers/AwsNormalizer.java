package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import java.util.UUID;

public class AwsNormalizer implements Normalizer {
  public NormalizedMetric normalize(UsageRecordModel record) {
    if (record == null) {
      return null;
    }

    String metricId = UUID.randomUUID().toString();
    String provider = "unknown";

    if (record.getProvider() != null) {
      provider = record.getProvider();
    }

    long usageStart = 0;
    if (record.getPeriodStart() != null) {
      usageStart = record.getPeriodStart().toEpochMilli();
    } else if (record.getTimestamp() != null) {
      usageStart = record.getTimestamp().toEpochMilli();
    }

    long usageEnd = 0;
    if (record.getPeriodEnd() != null) {
      usageEnd = record.getPeriodEnd().toEpochMilli();
    } else if (record.getTimestamp() != null) {
      usageEnd = record.getTimestamp().toEpochMilli();
    }

    String resourceId = record.getResourceId();
    String service = record.getServiceName();
    String serviceCategory = normalizeCategory(service);

    double usageAmount = record.getValue();
    String usageUnit = "unknown";
    if (record.getUnit() != null) {
      usageUnit = record.getUnit();
    }

    double effectiveCost = 0.0;
    String currency = "ZAR";
    String pricingModel = "on_demand";

    return new NormalizedMetric(
        metricId,
        provider,
        usageStart,
        usageEnd,
        resourceId,
        service,
        serviceCategory,
        usageAmount,
        usageUnit,
        effectiveCost,
        currency,
        pricingModel);
  }

  private static String normalizeCategory(String category) {
    if (category == null) {
      return "other";
    }

    String value = category.toLowerCase();

    if (value.equals("ec2")
        || value.equals("ecs")
        || value.equals("eks")
        || value.equals("lambda")) {
      return "compute";
    }

    if (value.equals("s3") || value.equals("ebs") || value.equals("efs")) {
      return "storage";
    }

    if (value.equals("rds") || value.equals("dynamodb") || value.equals("aurora")) {
      return "database";
    }

    return "other";
  }
}
