package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import static java.util.Map.entry;

import com.google.cloud.bigquery.FieldValueList;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ServiceNameNormalizer {
  private static final Map<String, String> skuServiceNameMap =
      Map.ofEntries(
          entry("1DF5-1F98-1DD1", "BigQuery Analysis"),
          entry("947D-3B46-7781", "Compute Engine Active Logical Storage"),
          entry("34CF-7D88-5D40", "Compute Engine Storage PD Snapshot in US"),
          entry(
              "D634-7DAF-EDA1",
              "Compute Engine Multi-regional Snapshot upload within North America"));

  public String normalizeServiceName(FieldValueList valueList) {

    String sku = valueList.get("sku_id").getStringValue();

    String skuServiceName = skuServiceNameMap.get(sku);

    if (skuServiceName != null) {
      return skuServiceName;
    }

    return valueList.get("service_description").getStringValue();
  }
}
