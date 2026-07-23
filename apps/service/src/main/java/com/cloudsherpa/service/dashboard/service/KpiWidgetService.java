package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.KpiCharge;
import com.cloudsherpa.lib.entities.WidgetKpi;
import com.cloudsherpa.lib.repositories.KpiChargeRepository;
import com.cloudsherpa.lib.repositories.WidgetKpiRepository;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class KpiWidgetService {
  private final WidgetKpiRepository widgetKpiRepository;
  private final KpiChargeRepository kpiChargeRepository;

  public KpiWidgetService(
      WidgetKpiRepository widgetKpiRepository, KpiChargeRepository kpiChargeRepository) {
    this.widgetKpiRepository = widgetKpiRepository;
    this.kpiChargeRepository = kpiChargeRepository;
  }

  public void createKpiWidget(KpiWidgetDTO kpiWidgetDTO) {
    UUID kpiId = UUID.randomUUID();
    UUID kpiChargesUuid = UUID.randomUUID();

    WidgetKpi widgetKpi =
        new WidgetKpi(kpiId, kpiWidgetDTO.id(), kpiWidgetDTO.aggregationWindowDays());
    widgetKpiRepository.save(widgetKpi);

    for (String chargeId : kpiWidgetDTO.chargeIds()) {
      KpiCharge kpiCharge = new KpiCharge(kpiChargesUuid, kpiId, chargeId);
      kpiChargeRepository.save(kpiCharge);
    }
  }

  public void updateKpiWidget(KpiWidgetConfigUpdateDTO kpiWidgetDTO) {
    // stubbed
  }
}
