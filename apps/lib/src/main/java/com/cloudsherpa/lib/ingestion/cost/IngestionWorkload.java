package com.cloudsherpa.lib.ingestion.cost;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.Map;

public record IngestionWorkload(
    ProviderEnum provider,
    long totalActiveResources,
    long totalMetricSeries,
    Map<IngestionResourceGroupKey, Long> resourcesByGroup,
    Map<String, Integer> metricsByServiceType) {

  public record IngestionResourceGroupKey(
      String serviceType,
      String region) {
  }
}
