package com.cloudsherpa.ingestion.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.cloudsherpa.ingestion.connector.AwsConnector;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.AwsNormalizer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// This class ties the Connector and Normalizer together, then sends the result to the Analytics service.
@Service
public class CloudUsageService 
{
    @Autowired
    private AwsConnector awsConnector;
    
    private final AwsNormalizer normalizer = new AwsNormalizer();

    // RestTemplate is Spring's standard HTTP client for making requests to external APIs
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${analytics.service.url}")
    private String analyticsServiceUrl;

    public void ingestAndProcessMetrics() 
    {
        // Get the raw string data from the connector
        List<Map<String, String>> rawMetrics = awsConnector.fetchRawData();
        
        // Mock environment ID (in production, this would come from user config)
        UUID environmentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Loop through every piece of raw data
        for (Map<String, String> rawMetricRow : rawMetrics) 
        {
            // Normalize the raw string into a clean Java object
            NormalizedMetric normalized = normalizer.normalize(rawMetricRow);
            
            if (normalized != null) 
            {
                // Send the clean object to the Analytics service
                sendToAnalyticsService(environmentId, normalized);
            }
        }
    }

    private void sendToAnalyticsService(UUID environmentId, NormalizedMetric metric) 
    {
        // Repackage the NormalizedMetric into a Map (which RestTemplate easily converts to JSON)
        // matching the structure expected by the AnalyticsController's MetricDTO.
        try 
        {
            Map<String, Object> payload = new HashMap<>();
            payload.put("environmentId", environmentId);
            payload.put("resourceId", metric.getResourceId());
            payload.put("serviceCategory", metric.getServiceCategory());
            payload.put("usageAmount", BigDecimal.valueOf(metric.getUsageAmount()));
            payload.put("usageUnit", metric.getUsageUnit());
            payload.put("costAmount", BigDecimal.valueOf(metric.getEffectiveCost()));
            payload.put("currency", metric.getCurrency());
            
            // Execute an HTTP POST request to the Analytics service, sending the JSON payload.
            restTemplate.postForObject(analyticsServiceUrl, payload, String.class);
        } 
        catch (Exception e) 
        {
            System.err.println(e.getMessage());
        }
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
            "OnDemand"
        );

        System.out.println("Ingested normalized metric: " + metric.getMetricId());
        return metric;
    }
}