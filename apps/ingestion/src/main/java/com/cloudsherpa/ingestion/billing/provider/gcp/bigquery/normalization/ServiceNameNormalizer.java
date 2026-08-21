package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.google.cloud.bigquery.FieldValueList;
import org.springframework.stereotype.Component;

@Component
public class ServiceNameNormalizer {
  public String normalizeServiceName(FieldValueList valueList) {

    return valueList.get("service_description").getStringValue()
        + " "
        + valueList.get("sku_description").getStringValue();
  }
}
