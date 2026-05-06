// The service class contains the business logic and uses the repository to perform
// operations on the database.
package com.cloudsherpa.ingestion.normalization.persistence.service;

import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.persistence.entity.EnvironmentReference;
import com.cloudsherpa.ingestion.normalization.persistence.entity.NormalizedMetrics;
import com.cloudsherpa.ingestion.normalization.persistence.repository.EnvironmentReferenceRepository;
import com.cloudsherpa.ingestion.normalization.persistence.repository.NormalizedMetricsRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SherpaDbPersistenceService {
  // Dependency Injection
  @Autowired private EnvironmentReferenceRepository environmentRepo;

  @Autowired private NormalizedMetricsRepository metricsRepo;

  // Use @Transactional when we are modifying a database in more than 1 place
  // So that if 1 step succeeds and the other one fails, the data doesn't end up half-written
  @Transactional
  public void recordMetric(UUID environmentId, NormalizedMetric metric) {
    EnvironmentReference environment = environmentRepo.getReferenceById(environmentId);

    // Create the new entity representing the row in the normalized_metrics table.
    NormalizedMetrics newMetric =
        new NormalizedMetrics(
            OffsetDateTime.now(),
            environment,
            metric.getResourceId(),
            metric.getServiceCategory(),
            BigDecimal.valueOf(metric.getUsageAmount()),
            metric.getUsageUnit(),
            BigDecimal.valueOf(metric.getEffectiveCost()),
            metric.getCurrency());

    // SQL insert statement
    // The actual database insertion. Spring Data JPA translates this into:
    // "INSERT INTO normalized_metrics (recorded_at, environment_id, ...) VALUES (...)"
    // Because an INSERT happens here, PostgreSQL immediately executes the `metric_notify_trigger`
    // defined in sherpadb-schema.sql, broadcasting the JSON event.
    metricsRepo.save(newMetric);
  }
}
