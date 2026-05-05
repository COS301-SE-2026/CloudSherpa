package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.cloudsherpa.ingestion.connector.AwsConnector;
import com.cloudsherpa.ingestion.normalization.normalizers.AwsNormalizer;
import com.cloudsherpa.ingestion.normalization.persistence.service.SherpaDbPersistenceService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// This class ties the Connector and Normalizer together, then writes the result to SherpaDB.
@Service
public class CloudUsageService 
{
    @Autowired
    private AwsConnector awsConnector;

    @Autowired
    private SherpaDbPersistenceService sherpaDbPersistenceService;
    
    private final AwsNormalizer normalizer = new AwsNormalizer();

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
                // Write the clean object directly into SherpaDB
                writeToSherpaDb(environmentId, normalized);
            }
        }
    }

    private void writeToSherpaDb(UUID environmentId, NormalizedMetric metric) 
    {
        try 
        {
            sherpaDbPersistenceService.recordMetric(environmentId, metric);
        } 
        catch (Exception e) 
        {
            System.err.println(e.getMessage());
        }
    }

  // Temporary method used to test the ingestion and normalization flow.
  public NormalizedMetric sendMockEvent() {
    long now = System.currentTimeMillis();
    NormalizedMetric metric =
        new NormalizedMetric(
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
        return metric;
    }
}
