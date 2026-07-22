package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.OfferedMetric;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferedMetricRepository extends JpaRepository<OfferedMetric, UUID> {

  List<OfferedMetric> findByProvider(ProviderEnum provider);

  List<OfferedMetric> findByServiceType(String serviceType);

  List<OfferedMetric> findByProviderAndServiceType(
      ProviderEnum provider,
      String serviceType);
}
