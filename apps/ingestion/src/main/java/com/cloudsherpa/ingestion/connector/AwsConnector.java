package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

// Mock AWS connector class
@Component
public class AwsConnector implements CloudConnector {
  @Override
  public String getProviderName() {
    return "AWS";
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    return true;
  }

  @Override
  public List<String> getAllOfferedServices() {
    List<String> services = new ArrayList<>();
    services.add("AWS/EC2");
    services.add("AWS/ECS");
    services.add("AWS/EKS");
    services.add("AWS/Lambda");
    services.add("AWS/RDS");
    services.add("AWS/ElastiCache");
    services.add("AWS/OpenSearch");
    services.add("AWS/RedShift");

    return services;
  }

  @Override
  public List<ResourceDetail> getAllResources(CloudCredentials credentials, List<String> serviceTypes) {
    return new ArrayList<>();
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
