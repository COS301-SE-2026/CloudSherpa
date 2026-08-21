package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.google.cloud.bigquery.FieldValueList;
import org.springframework.stereotype.Component;

@Component
public class ServiceNameNormalizer {
  public String normalizeServiceName(FieldValueList valueList) {

    String skuDescription = valueList.get("sku_description").getStringValue();

    if (skuDescription.toLowerCase().contains("data transfer")) {
      return "Data Transfer";
    }

    return valueList.get("service_description").getStringValue() + " " + skuDescription;
  }
}
