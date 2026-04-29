// The service class contains the business logic and uses the repository to perform 
// operations on the database.
// Used https://medium.com/@bshiramagond/jpa-with-spring-boot-a-comprehensive-guide-with-examples-e07da6f3d385 for EnvironmentReferenceRepository

package com.cloudsherpa.analytics.service;

import com.cloudsherpa.analytics.entity.EnvironmentReference;
import com.cloudsherpa.analytics.entity.NormalizedMetrics;

import com.cloudsherpa.analytics.repository.EnvironmentReferenceRepository;
import com.cloudsherpa.analytics.repository.NormalizedMetricsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

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
        NormalizedMetrics newMetric = new NormalizedMetrics(OffsetDateTime.now(), environment, resourceId, serviceCategory, usageAmount, usageUnit, costAmount, currency);

        // SQL insert statement
        metricsRepo.save(newMetric);
    }
}