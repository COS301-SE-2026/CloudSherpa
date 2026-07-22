package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.ChartResource;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.repositories.ChartResourceRepository;
import com.cloudsherpa.lib.repositories.WidgetChartRepository;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChartWidgetService {
  private final WidgetChartRepository widgetChartRepository;
  private final ChartResourceRepository chartResourceRepository;

  public ChartWidgetService(WidgetChartRepository widgetChartRepository) {
    this.widgetChartRepository = widgetChartRepository;
    this.chartResourceRepository = chartResourceRepository;
  }

  public void createChartWidget(ChartWidgetDTO chartWidgetDto) {
    // stubbed
  }

  public void updateChartWidget(ChartWidgetConfigUpdateDTO chartWidgetDto) {
    // stubbed
  }

  public ChartWidgetDTO mapToChartWidgetDTO(UUID userId, Widget widget) {

    UUID resourceId = null;
    String metricType = null;

    List<ChartResource> resources = new ArrayList<>();
    //   chartResourceRepository.(widget.getId());
    if (!resources.isEmpty()) {
      resourceId = resources.get(0).getResourceId();
      metricType = resources.get(0).getMetricType();
    }

    return new ChartWidgetDTO(
        userId,
        widget.getId(),
        widget.getType(),
        widget.getDisplayName(),
        widget.getStartX(),
        widget.getStartY(),
        widget.getWidth(),
        widget.getHeight(),
        resourceId,
        metricType);
  }
}
