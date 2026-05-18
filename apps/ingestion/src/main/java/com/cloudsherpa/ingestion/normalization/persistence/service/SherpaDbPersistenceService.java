// The service class contains the business logic and uses the repository to perform
// operations on the database.
package com.cloudsherpa.ingestion.normalization.persistence.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.persistence.entity.NormalizedMetrics;
import com.cloudsherpa.ingestion.normalization.persistence.entity.Resource;
import com.cloudsherpa.ingestion.normalization.persistence.repository.NormalizedMetricsRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SherpaDbPersistenceService {

  @Autowired private NormalizedMetricsRepository metricsRepo;
  @Autowired private CloudInfrastructureService infrastructureService;

  // Use @Transactional when we are modifying a database in more than 1 place
  // So that if 1 step succeeds and the other one fails, the data doesn't end up half-written
  @Transactional
  public void recordMetric(NormalizedMetric metric, UsageRecordModel record, UUID userId) {

    Resource resource = infrastructureService.ensureInfrastructure(record, userId);
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
        new NormalizedMetrics(
            accountId,
            OffsetDateTime.now(),
            resourceUuid,
            metric.getMetricType(),
            metric.getMetricName(),
            BigDecimal.valueOf(metric.getMetricValue()),
            metric.getUnit(),
            metric.getCurrency(),
            periodStart,
            periodEnd);

    // SQL insert statement
    // The actual database insertion. Spring Data JPA translates this into:
    // "INSERT INTO normalized_metrics (recorded_at, environment_id, ...) VALUES (...)"
    // Because an INSERT happens here, PostgreSQL immediately executes the `metric_notify_trigger`
    // defined in sherpadb-schema.sql, broadcasting the JSON event.
    metricsRepo.save(newMetric);
  }
}
