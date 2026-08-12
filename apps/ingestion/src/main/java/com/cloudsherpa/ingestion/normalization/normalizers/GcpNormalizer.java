package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.time.Instant;
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

    String accountKey = resolveAccountKey(r);
    UUID accountUuid = parseUuid(accountKey);
    String resourceId = resolveResourceId(accountUuid, r);

    String metricName =
        prettifyGcpMetricName(r.getMetricName() != null ? r.getMetricName() : "unknown");
    String metricType = determineMetricType(metricName);

    double metricValue = r.getValue();
    String unit = normalizeGcpUnit(r.getUnit());
    String currency = null;

    long periodStart = resolveEpochMilli(r.getPeriodStart(), r.getTimestamp());
    long periodEnd = resolveEpochMilli(r.getPeriodEnd(), r.getTimestamp());

    return new NormalizedMetric.Builder()
        .metricId(UUID.randomUUID().toString())
        .resourceId(resourceId)
        .accountId(accountKey)
        .metricType(metricType)
        .metricName(metricName)
        .metricValue(metricValue)
        .unit(unit)
        .currency(currency)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .build();
  }

  private String resolveAccountKey(UsageRecordModel r) {
    String accountKey = r.getAccountId();
    if (accountKey == null || accountKey.trim().isEmpty()) {
      accountKey = r.getProjectId();
    }
    return accountKey;
  }

  private UUID parseUuid(String accountKey) {
    if (accountKey == null || accountKey.trim().isEmpty()) {
      return null;
    }
    try {
      return UUID.fromString(accountKey);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private String resolveResourceId(UUID accountUuid, UsageRecordModel r) {
    if (accountUuid == null) {
      return null;
    }
    return resourceRepository
        .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            accountUuid, r.getServiceName(), r.getResourceId(), r.getRegion())
        .map(Resource::getId)
        .map(UUID::toString)
        .orElse(null);
  }

  private long resolveEpochMilli(Instant primary, Instant fallback) {
    if (primary != null) {
      return primary.toEpochMilli();
    }
    if (fallback != null) {
      return fallback.toEpochMilli();
    }
    return 0;
  }

  private String determineMetricType(String metricName) {
    String mnLower = metricName.toLowerCase();
    if (mnLower.contains("cost") || mnLower.contains("charge") || mnLower.contains("billing")) {
      return "cost";
    }
    if (mnLower.contains("latency")
        || mnLower.contains("duration")
        || mnLower.contains("error")
        || mnLower.contains("throttle")
        || mnLower.contains("utilization")) {
      return "performance";
    }
    return "usage";
  }

  private String normalizeGcpUnit(String gcpUnit) {
    if (gcpUnit == null) {
      return "unknown";
    }

    String t = gcpUnit.trim();
    switch (t) {
      case "By":
        return "bytes";
      case "s":
        return "seconds";
      case "Percent", "percent", "10^2.%":
        return "percent";
      case "Count", "1":
        return "count";
      default:
        return t.toLowerCase();
    }
  }

  private String prettifyGcpMetricName(String fullMetricName) {
    if (fullMetricName == null || fullMetricName.isEmpty()) {
      return "unknown";
    }

    String[] parts = fullMetricName.split("/");
    String lastPart = parts[parts.length - 1];

    return convertSnakeToCamel(lastPart);
  }

  private String convertSnakeToCamel(String snake) {
    if (snake == null || snake.isEmpty()) return snake;

    StringBuilder camel = new StringBuilder();
    boolean capitalizeNext = true;

    for (char c : snake.toCharArray()) {
      if (c == '_') {
        capitalizeNext = true;
      } else {
        if (capitalizeNext) {
          camel.append(Character.toUpperCase(c));
          capitalizeNext = false;
        } else {
          camel.append(c);
        }
      }
    }

    return camel.toString();
  }
}
