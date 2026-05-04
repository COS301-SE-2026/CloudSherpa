package com.cloudsherpa.ingestion.service;

import org.springframework.stereotype.Service;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.BillingCapable;
import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.connector.UsageCapable;
import com.cloudsherpa.ingestion.models.BillingRecordModel;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermediary between the CloudUsageController and the ingestion pipeline.
 */
@Service
public class CloudUsageService {
    private final CloudConnectorFactory factory;

    public CloudUsageService(CloudConnectorFactory factory) {
        this.factory = factory;
    }

    public IngestionResult ingest(IngestionRequestEvent request) {

        List<UsageRecordModel> usageResults = new ArrayList<>();
        List<BillingRecordModel> billingResults = new ArrayList<>();

        for (AccountScope scope : request.getScopes()) {

            CloudConnector connector = factory.getConnector(scope.getProvider());

            if (request.isIncludeUsage() && connector instanceof UsageCapable usageCapable) {
                usageResults.addAll(
                        usageCapable.fetchUsage(scope, request));
            }

            if (request.isIncludeBilling() && connector instanceof BillingCapable billingCapable) {
                billingResults.addAll(
                        billingCapable.fetchBilling(scope, request));
            }
        }

        return new IngestionResult(usageResults, billingResults);
    }

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
                "OnDemand");

        System.out.println("Ingested normalized metric: " + metric.getMetricId());
        return metric;
    }
}
