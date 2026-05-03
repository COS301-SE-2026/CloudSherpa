// The service class contains the business logic and uses the repository to perform 
// operations on the database.
// Used https://medium.com/@bshiramagond/jpa-with-spring-boot-a-comprehensive-guide-with-examples-e07da6f3d385 for EnvironmentReferenceRepository

package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.service.analytics.entity.EnvironmentReference;
import com.cloudsherpa.service.analytics.entity.NormalizedMetrics;

import com.cloudsherpa.service.analytics.repository.EnvironmentReferenceRepository;
import com.cloudsherpa.service.analytics.repository.NormalizedMetricsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class AnalyticsPersistenceService 
{
    // Dependency Injection
    @Autowired
    private EnvironmentReferenceRepository environmentRepo;

    @Autowired
    private NormalizedMetricsRepository metricsRepo;

    // Use @Transactional when we are modifying a database in more than 1 place
    // So that if 1 step succeeds and the other one fails, the data doesn't end up half-written
    @Transactional
    public void recordMetric(UUID environmentId, String resourceId, String serviceCategory, BigDecimal usageAmount, String usageUnit, BigDecimal costAmount, String currency) 
    {
        EnvironmentReference environment = environmentRepo.getReferenceById(environmentId);

        // Create the new entity representing the row in the normalized_metrics table.
        NormalizedMetrics newMetric = new NormalizedMetrics(OffsetDateTime.now(), environment, resourceId, serviceCategory, usageAmount, usageUnit, costAmount, currency);

        // SQL insert statement
        // The actual database insertion. Spring Data JPA translates this into:
        // "INSERT INTO normalized_metrics (recorded_at, environment_id, ...) VALUES (...)"
        // Because an INSERT happens here, PostgreSQL immediately executes the `metric_notify_trigger` 
        // defined in analytics-schema.sql, broadcasting the JSON event.
        metricsRepo.save(newMetric);
    }

    //Fetches all metrics recorded between the specified start and end times.
    public List<NormalizedMetrics> getMetricsInTimeWindow(OffsetDateTime startTime, OffsetDateTime endTime) 
    {
        List<NormalizedMetrics> metrics = metricsRepo.findByRecordedAtBetween(startTime, endTime);
        
        // Note: Because we are fetching a List of objects, we can easily perform additional 
        // business logic right here in Java using Streams (e.g., calculating total costs) 
        // without needing to write complex SQL aggregation scripts.
        
        return metrics;
    }
}