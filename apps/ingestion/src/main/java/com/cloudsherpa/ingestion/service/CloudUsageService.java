package com.cloudsherpa.ingestion.service;

import org.springframework.stereotype.Service;

import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;

/**
 * Intermediary between the CloudUsageController and the ingestion pipeline.
 */
@Service
public class CloudUsageService {

    // Temporary method used to test the ingestion and normalization flow.
    public NormalizedMetric sendMockEvent() {
        long now = System.currentTimeMillis();
        NormalizedMetric metric = new NormalizedMetric(
            "mock-metric-1",
            "AWS",
            now,
            now,
            "mock-resource-1",
            "EC2",
            "Compute",
            42.0,
            "Hours",
            12.75,
            "USD",
            "OnDemand"
        );

        System.out.println("Ingested normalized metric: " + metric.getMetricId());
        return metric;
    }
}
