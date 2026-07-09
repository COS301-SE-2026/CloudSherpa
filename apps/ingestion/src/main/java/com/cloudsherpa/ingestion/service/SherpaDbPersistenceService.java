// The service class contains the business logic and uses the repository to perform
// operations on the database.
package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.lib.entities.MetricTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SherpaDbPersistenceService {

  private final NormalizedMetricsRepository metricsRepo;
  private final CloudInfrastructureService infrastructureService;
  private static final Pattern TENANT_SCHEMA_PATTERN = Pattern.compile("^tenant_[a-f0-9_]{36}$");

  @PersistenceContext private EntityManager entityManager;

  public SherpaDbPersistenceService(
      NormalizedMetricsRepository metricsRepo, CloudInfrastructureService infrastructureService) {
    this.metricsRepo = metricsRepo;
    this.infrastructureService = infrastructureService;
  }

  private void setTenantSchema(String tenantIdHeader) {
    String schema = normalizeTenantSchema(tenantIdHeader);
    entityManager.createNativeQuery("SET search_path TO " + schema + ", public").executeUpdate();
  }

  // Use @Transactional when we are modifying a database in more than 1 place
  // So that if 1 step succeeds and the other one fails, the data doesn't end up half-written
  @Transactional
  public void recordMetric(
      NormalizedMetric metric, UsageRecordModel r, UUID userId, String tenantId) {

    setTenantSchema(tenantId);

    Resource resource = infrastructureService.ensureInfrastructure(r, userId);
    UUID resourceUuid = resource.getId();

    OffsetDateTime periodStart = null;
    OffsetDateTime periodEnd = null;

    if (metric.getPeriodStart() > 0) {
      periodStart =
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(metric.getPeriodStart()), ZoneOffset.UTC);
    }

    if (metric.getPeriodEnd() > 0) {
      periodEnd =
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(metric.getPeriodEnd()), ZoneOffset.UTC);
    }

    MetricTypeEnum metricTypeEnum;
    try {
      metricTypeEnum = MetricTypeEnum.valueOf(metric.getMetricType().toLowerCase());
    } catch (Exception ex) {
      metricTypeEnum = MetricTypeEnum.usage;
    }

    // Create the new entity representing the row in the normalized_metrics table.
    NormalizedMetrics newMetric =
        new NormalizedMetrics.Builder()
            .resourceId(resourceUuid)
            .recordedAt(OffsetDateTime.now())
            .metricType(metricTypeEnum)
            .metricName(metric.getMetricName())
            .metricValue(BigDecimal.valueOf(metric.getMetricValue()))
            .unit(metric.getUnit())
            .currency(metric.getCurrency())
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .build();

    // SQL insert statement
    // The actual database insertion. Spring Data JPA translates this into:
    // "INSERT INTO normalized_metrics (recorded_at, environment_id, ...) VALUES (...)"
    // Because an INSERT happens here, PostgreSQL immediately executes the `metric_notify_trigger`
    // defined in sherpadb-schema.sql, broadcasting the JSON event.
    metricsRepo.save(newMetric);
  }

  private String normalizeTenantSchema(String tenantIdHeader) {
    if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
      throw new IllegalArgumentException("tenant-id header is required");
    }

    String trimmed = tenantIdHeader.trim().toLowerCase();
    String schema = "";

    if (trimmed.startsWith("tenant_")) {
      schema = trimmed;
    } else {
      schema = "tenant_" + trimmed.replace("-", "_");
    }

    if (!TENANT_SCHEMA_PATTERN.matcher(schema).matches()) {
      throw new IllegalArgumentException("Invalid tenant-id format: " + tenantIdHeader);
    }

    return schema;
  }
}
