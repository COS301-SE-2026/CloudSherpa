package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
public class GcpNormalizer implements Normalizer {
  private final ResourceRepository resourceRepository;

  public GcpNormalizer(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  public NormalizedMetric normalize(UsageRecordModel r) {
    if (r == null) {
      return null;
    }

    return null;
    // return new NormalizedMetric.Builder()
    //     .metricId(metricId)
    //     .resourceId(resourceId)
    //     .accountId(accountId)
    //     .metricType(metricType)
    //     .metricName(metricName)
    //     .metricValue(metricValue)
    //     .unit(unit)
    //     .currency(currency)
    //     .periodStart(periodStart)
    //     .periodEnd(periodEnd)
    //     .build();
  }
}
