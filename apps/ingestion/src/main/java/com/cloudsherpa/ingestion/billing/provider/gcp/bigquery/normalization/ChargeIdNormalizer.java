package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import static java.util.Map.entry;

import com.google.cloud.bigquery.FieldValueList;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ChargeIdNormalizer {

  private final ServiceNameNormalizer serviceNameNormalizer;

  public ChargeIdNormalizer(ServiceNameNormalizer serviceNameNormalizer) {
    this.serviceNameNormalizer = serviceNameNormalizer;
  }

  private static final Map<String, String> skuChargeNameMap =
      Map.ofEntries(
          entry("1DF5-1F98-1DD1", "%%%BigQuery Analysis"),
          entry("34CF-7D88-5D40", "%%%Compute Engine Storage PD Snapshot in US"));

  public String normalizeChargeId(FieldValueList valueList) {

    String sku = valueList.get("sku_id").getStringValue();

    String skuChargeId = skuChargeNameMap.get(sku);

    if (skuChargeId != null) {
      return skuChargeId;
    }

    if (valueList.get("resource_name").isNull()) {
      return "NoResourceId" + "%%%" + serviceNameNormalizer.normalizeServiceName(valueList);
    }

    return valueList.get("resource_name").getStringValue()
        + "%%%"
        + serviceNameNormalizer.normalizeServiceName(valueList);
  }
}
