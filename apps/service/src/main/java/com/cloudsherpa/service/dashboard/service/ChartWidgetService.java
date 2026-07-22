package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.ChartResource;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.entities.WidgetChart;
import com.cloudsherpa.lib.repositories.ChartResourceRepository;
import com.cloudsherpa.lib.repositories.DashboardWidgetRepository;
import com.cloudsherpa.lib.repositories.WidgetChartRepository;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetDTO;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChartWidgetService {
  private final WidgetChartRepository widgetChartRepository;
  private final ChartResourceRepository chartResourceRepository;
  private final DashboardWidgetRepository dashboardWidgetRepository;

  public ChartWidgetService(
      WidgetChartRepository widgetChartRepository,
      ChartResourceRepository chartResourceRepository,
      DashboardWidgetRepository dashboardWidgetRepository) {
    this.widgetChartRepository = widgetChartRepository;
    this.chartResourceRepository = chartResourceRepository;
    this.dashboardWidgetRepository = dashboardWidgetRepository;
  }

  @Transactional
  public void createChartWidget(ChartWidgetDTO chartWidgetDto, UUID dashboardId) {

    UUID widgetChartId = UUID.randomUUID();
    UUID chartResourceId = UUID.randomUUID();

    Widget newWidget =
        new Widget(
            chartWidgetDto.id(),
            dashboardId,
            chartWidgetDto.widgetType(),
            chartWidgetDto.startX(),
            chartWidgetDto.startY(),
            chartWidgetDto.width(),
            chartWidgetDto.height(),
            chartWidgetDto.displayName());
    WidgetChart newWidgetChart =
        new WidgetChart(widgetChartId, chartWidgetDto.id(), chartWidgetDto.chartType());
    ChartResource chartResource =
        new ChartResource(
            chartResourceId,
            widgetChartId,
            chartWidgetDto.resourceId(),
            chartWidgetDto.metricType());

    dashboardWidgetRepository.save(newWidget);
    widgetChartRepository.save(newWidgetChart);
    chartResourceRepository.save(chartResource);
  }

  public void updateChartWidget(ChartWidgetConfigUpdateDTO chartWidgetDto) {}

  public ChartWidgetDTO mapToChartWidgetDTO(UUID userId, ChartWidgetDTO widget) {

    UUID resourceId = null;
    String metricType = null;
    List<WidgetChart> chartIdLookup = widgetChartRepository.findByWidgetId(widget.id());

    if (chartIdLookup.isEmpty()) {
      throw new IllegalStateException(
          "No chart persistence found for widget: " + widget.id().toString());
    }

    UUID chartId = chartIdLookup.get(0).getId();

    List<ChartResource> resources = chartResourceRepository.findByWidgetChartId(chartId);

    if (!resources.isEmpty()) {
      resourceId = resources.get(0).getResourceId();
      metricType = resources.get(0).getMetricType();
    }

    return new ChartWidgetDTO(
        userId,
        widget.id(),
        widget.widgetType(),
        widget.displayName(),
        widget.startX(),
        widget.startY(),
        widget.width(),
        widget.height(),
        widget.chartType(),
        resourceId,
        metricType);
  }
}
