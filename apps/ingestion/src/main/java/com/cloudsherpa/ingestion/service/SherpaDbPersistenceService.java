// The service class contains the business logic and uses the repository to perform
// operations on the database.
package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SherpaDbPersistenceService {

  private final NormalizedMetricsRepository metricsRepo;
  private final CloudInfrastructureService infrastructureService;

  public SherpaDbPersistenceService(
      NormalizedMetricsRepository metricsRepo, CloudInfrastructureService infrastructureService) {
    this.metricsRepo = metricsRepo;
    this.infrastructureService = infrastructureService;
  }

  // Use @Transactional when we are modifying a database in more than 1 place
  // So that if 1 step succeeds and the other one fails, the data doesn't end up half-written
  @Transactional
  public void recordMetric(NormalizedMetric metric, UsageRecordModel r, UUID userId) {

    Resource resource = infrastructureService.ensureInfrastructure(r, userId);
    UUID accountId = resource.getAccountId();
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

    // Create the new entity representing the row in the normalized_metrics table.
    NormalizedMetrics newMetric =
        new NormalizedMetrics.Builder()
            .accountId(accountId)
            .recordedAt(OffsetDateTime.now())
            .resourceId(resourceUuid)
            .metricType(metric.getMetricType())
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
}
