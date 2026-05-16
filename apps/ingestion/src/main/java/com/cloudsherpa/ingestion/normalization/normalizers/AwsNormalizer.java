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

    String provider = "unknown";
    if (r.getProvider() != null) {
      provider = r.getProvider();
    }

    long usageStart = 0;
    if (r.getPeriodStart() != null) {
      usageStart = r.getPeriodStart().toEpochMilli();
    } else if (r.getTimestamp() != null) {
      usageStart = r.getTimestamp().toEpochMilli();
    }

    long usageEnd = 0;
    if (r.getPeriodEnd() != null) {
      usageEnd = r.getPeriodEnd().toEpochMilli();
    } else if (r.getTimestamp() != null) {
      usageEnd = r.getTimestamp().toEpochMilli();
    }

    String service = "unknown";
    if (r.getServiceName() != null) {
      service = r.getServiceName();
    }

    String shortService = service;
    if (service.contains("/")) {
      shortService = service.substring(service.indexOf('/') + 1);
    }

    String serviceCategory = normalizeCategory(shortService);

    String resourceId = r.getResourceId();

    double usageAmount = r.getValue();

    String usageUnit = "None";
    if (r.getUnit() != null) {
      usageUnit = r.getUnit();
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
