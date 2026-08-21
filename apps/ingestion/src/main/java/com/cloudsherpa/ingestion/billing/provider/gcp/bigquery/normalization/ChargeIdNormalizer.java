package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import static java.util.Map.entry;

import com.google.cloud.bigquery.FieldValueList;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ChargeIdNormalizer {
  private static final Map<String, String> skuChargeNameMap =
      Map.ofEntries(
          entry("1DF5-1F98-1DD1", "BigQueryAnalysis"),
          entry("34CF-7D88-5D40", "ComputeEngineStoragePDSnapshotinUS"));

  public String normalizeChargeId(FieldValueList valueList) {

    String sku = valueList.get("sku_id").getStringValue();

    String skuChargeId = skuChargeNameMap.get(sku);

    if (skuChargeId != null) {
      return skuChargeId;
    }

    if (valueList.get("resource_global_name").isNull()) {
      return "NoResourceId";
    }

    return valueList.get("resource_global_name").getStringValue();
  }
}
