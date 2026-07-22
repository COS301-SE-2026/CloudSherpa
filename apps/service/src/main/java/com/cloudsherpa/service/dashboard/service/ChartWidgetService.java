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

  @Transactional
  public void updateChartWidget(ChartWidgetConfigUpdateDTO updateChartWidgetDto) {
    WidgetChart widgetChart = getWidgetChartByWidgetId(updateChartWidgetDto.id());
    widgetChart.setChartType(updateChartWidgetDto.chartType());
    List<ChartResource> resources =
        chartResourceRepository.findByWidgetChartId(widgetChart.getId());

    if (resources.isEmpty()) {
      throw new IllegalStateException("No chart resource for chart id " + widgetChart.getId());
    }

    ChartResource resource = resources.get(0);
    resource.setMetricType(updateChartWidgetDto.metricType());
    resource.setResourceId(updateChartWidgetDto.resourceId());

    widgetChartRepository.save(widgetChart);
    chartResourceRepository.save(resource);
  }

  public ChartWidgetDTO mapToChartWidgetDTO(UUID userId, ChartWidgetDTO widget) {

    UUID resourceId = null;
    String metricType = null;
    UUID chartId = getWidgetChartByWidgetId(widget.id()).getId();

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

  private WidgetChart getWidgetChartByWidgetId(UUID widgetId) {
    List<WidgetChart> widgetChartLookup = widgetChartRepository.findByWidgetId(widgetId);

    if (widgetChartLookup.isEmpty()) {
      throw new IllegalStateException("Failed to find chart widget with id " + widgetId.toString());
    }

    return widgetChartLookup.get(0);
  }
}
