package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.KpiCharge;
import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.entities.WidgetKpi;
import com.cloudsherpa.lib.repositories.KpiChargeRepository;
import com.cloudsherpa.lib.repositories.WidgetKpiRepository;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

  @Transactional
  public void createKpiWidget(KpiWidgetDTO kpiWidgetDTO) {
    UUID kpiId = UUID.randomUUID();

    WidgetKpi widgetKpi =
        new WidgetKpi(kpiId, kpiWidgetDTO.id(), kpiWidgetDTO.aggregationWindowDays());
    widgetKpiRepository.save(widgetKpi);

    for (String chargeId : kpiWidgetDTO.chargeIds()) {
      createKpiCharge(kpiId, chargeId);
    }
  }

  @Transactional
  public void updateKpiWidget(KpiWidgetConfigUpdateDTO kpiWidgetDTO) {
    WidgetKpi widgetKpi = getWidgetKpiByWidgetId(kpiWidgetDTO.id());
    widgetKpi.setAggregation(kpiWidgetDTO.aggregationWindowDays());
    widgetKpiRepository.save(widgetKpi);

    // update chargeIds
    List<KpiCharge> kpiChargesLookup = kpiChargeRepository.findByWidgetKpiId(widgetKpi.getId());
    List<String> pendingCharges = new ArrayList<>(kpiWidgetDTO.chargeIds());

    Iterator<KpiCharge> kpiChargeIt = kpiChargesLookup.iterator();

    // Remove entries in database that are not in request, remove charge from pending charges if it
    // already exists
    // in the db
    while (kpiChargeIt.hasNext()) {
      KpiCharge currentCharge = kpiChargeIt.next();
      if (!pendingCharges.contains(currentCharge.getChargeId())) {
        kpiChargeRepository.delete(currentCharge);
      } else {
        pendingCharges.remove(currentCharge.getChargeId());
      }
    }

    for (String newCharge : pendingCharges) {
      createKpiCharge(widgetKpi.getId(), newCharge);
    }
  }

  private WidgetKpi getWidgetKpiByWidgetId(UUID widgetId) {
    List<WidgetKpi> widgetKpiLookup = widgetKpiRepository.findByWidgetId(widgetId);

    if (widgetKpiLookup.isEmpty()) {
      throw new IllegalStateException(
          "Could not find KPI widget linked to widget " + widgetId.toString());
    }

    return widgetKpiLookup.get(0);
  }

  @Transactional
  private KpiCharge createKpiCharge(UUID widgetKpiId, String chargeId) {
    UUID kpiChargesId = UUID.randomUUID();
    KpiCharge kpiCharge = new KpiCharge(kpiChargesId, widgetKpiId, chargeId);
    kpiChargeRepository.save(kpiCharge);
    return kpiCharge;
  }

  public KpiWidgetDTO mapToKpiWidgetDTO(Widget widget) {
    WidgetKpi widgetKpi = getWidgetKpiByWidgetId(widget.getId());
    List<KpiCharge> kpiChargesLookup = kpiChargeRepository.findByWidgetKpiId(widget.getId());

    List<String> kpiCharges = kpiChargesLookup.stream().map(KpiCharge::getChargeId).toList();

    return new KpiWidgetDTO(
        widget.getId(),
        TypeEnum.KPI,
        widget.getDisplayName(),
        widget.getStartX(),
        widget.getStartY(),
        widget.getWidth(),
        widget.getHeight(),
        kpiCharges,
        widgetKpi.getAggregation());
  }
}
