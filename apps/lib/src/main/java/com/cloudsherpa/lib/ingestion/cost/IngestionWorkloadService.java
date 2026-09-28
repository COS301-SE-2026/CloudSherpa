package com.cloudsherpa.lib.ingestion.cost;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.OfferedMetric;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.OfferedMetricRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngestionWorkloadService {

  private final ResourceRepository resourceRepository;
  private final OfferedMetricRepository offeredMetricRepository;

  public IngestionWorkloadService(
      ResourceRepository resourceRepository,
      OfferedMetricRepository offeredMetricRepository) {
    this.resourceRepository = resourceRepository;
    this.offeredMetricRepository = offeredMetricRepository;
  }

  public IngestionWorkload buildWorkload(CloudAccount account) {

    List<Resource> activeResources = resourceRepository.findByAccountIdAndStatus(
        account.getId(),
        StatusEnum.active);

    Map<String, Integer> metricsByServiceType = new HashMap<>();

    for (OfferedMetric metric : offeredMetricRepository.findByProvider(
        account.getConnection().getProvider())) {

      metricsByServiceType.merge(
          metric.getServiceType(),
          1,
          Integer::sum);
    }

    Map<IngestionWorkload.IngestionResourceGroupKey, Long> resourcesByGroup = new HashMap<>();

    for (Resource resource : activeResources) {
      IngestionWorkload.IngestionResourceGroupKey key = new IngestionWorkload.IngestionResourceGroupKey(
          resource.getResourceType(),
          resource.getRegion());

      resourcesByGroup.merge(
          key,
          1L,
          Long::sum);
    }

    long totalMetricSeries = 0;

    for (Map.Entry<IngestionWorkload.IngestionResourceGroupKey, Long> entry : resourcesByGroup.entrySet()) {

      int metricCount = metricsByServiceType.getOrDefault(
          entry.getKey().serviceType(),
          0);

      totalMetricSeries += entry.getValue() * metricCount;
    }

    return new IngestionWorkload(
        account.getConnection().getProvider(),
        activeResources.size(),
        totalMetricSeries,
        Map.copyOf(resourcesByGroup),
        Map.copyOf(metricsByServiceType));
  }
}
