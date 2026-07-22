package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.repositories.WidgetKpiRepository;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import org.springframework.stereotype.Service;

@Service
public class KpiWidgetService {
  private final WidgetKpiRepository widgetKpiRepository;

  public KpiWidgetService(WidgetKpiRepository widgetKpiRepository) {
    this.widgetKpiRepository = widgetKpiRepository;
  }

  public void createKpiWidget(KpiWidgetDTO kpiWidgetDTO) {
    // stubbed
  }

  public void updateKpiWidget(KpiWidgetConfigUpdateDTO kpiWidgetDTO) {
    // stubbed
  }
}
