package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.KpiCharge;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiChargeRepository extends JpaRepository<KpiCharge, UUID> {
  List<KpiCharge> findByWidgetKpiId(UUID widgetKpiId);
  List<KpiCharge> findByChargeId(String chargeId);
}