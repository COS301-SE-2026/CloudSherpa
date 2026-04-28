// Refer to EnvironmentReferenceRepository.java for most documentation

package com.cloudsherpa.analytics.repository;

import com.cloudsherpa.analytics.entity.NormalizedMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NormalizedMetricsRepository extends JpaRepository<NormalizedMetrics, UUID> 
{

}