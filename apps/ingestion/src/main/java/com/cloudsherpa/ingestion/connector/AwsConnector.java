package com.cloudsherpa.ingestion.connector;

import org.springframework.stereotype.Component;

import com.cloudsherpa.ingestion.models.IngestionRequestEvent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

// Mock AWS connector class
@Component
public class AwsConnector implements CloudConnector {
    @Override
    public String getProviderName() {
        return "AWS";
    }

    public void ingest(IngestionRequestEvent request) {
    } // Mock implementation

    @Override
    public boolean testConnection(CloudCredentials credentials) {
        return true;
    }

    public List<Map<String, String>> fetchRawData() {
        List<Map<String, String>> rawMetrics = new ArrayList<>();

        // Mocking a row from an AWS Cost and Usage Report (CUR) CSV
        Map<String, String> row1 = new HashMap<>();
        row1.put("lineItem_ResourceId", "i-0123456789abcdef0");
        row1.put("lineItem_ProductCode", "AmazonEC2");
        row1.put("lineItem_UsageAmount", "42.5");
        row1.put("pricing_unit", "Hrs");
        row1.put("lineItem_UnblendedCost", "12.75");
        row1.put("lineItem_LineItemType", "Usage");

        // Generate an ISO-8601 timestamp string so your normalizer's parseTime method
        // works
        String nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString();
        row1.put("lineItem_UsageStartDate", nowIso);
        row1.put("lineItem_UsageEndDate", nowIso);

        rawMetrics.add(row1);

        return rawMetrics;
    }
}
