package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.ChartResource;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.entities.WidgetChart;
import com.cloudsherpa.lib.repositories.ChartResourceRepository;
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

  public ChartWidgetService(
      WidgetChartRepository widgetChartRepository,
      ChartResourceRepository chartResourceRepository) {
    this.widgetChartRepository = widgetChartRepository;
    this.chartResourceRepository = chartResourceRepository;
  }

  @Transactional
  public void createChartWidget(ChartWidgetDTO chartWidgetDto) {

    UUID widgetChartId = UUID.randomUUID();
    UUID chartResourceId = UUID.randomUUID();

    WidgetChart newWidgetChart =
        new WidgetChart(widgetChartId, chartWidgetDto.id(), chartWidgetDto.chartType());
    ChartResource chartResource =
        new ChartResource(
            chartResourceId,
            widgetChartId,
            chartWidgetDto.resourceId(),
            chartWidgetDto.metricType());

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

  public ChartWidgetDTO mapToChartWidgetDTO(Widget widget) {

    UUID resourceId = null;
    String metricType = null;
    WidgetChart chart = getWidgetChartByWidgetId(widget.getId());

    List<ChartResource> resources = chartResourceRepository.findByWidgetChartId(chart.getId());

    if (!resources.isEmpty()) {
      resourceId = resources.get(0).getResourceId();
      metricType = resources.get(0).getMetricType();
    }

    return new ChartWidgetDTO(
        widget.getId(),
        widget.getType(),
        widget.getDisplayName(),
        widget.getStartX(),
        widget.getStartX(),
        widget.getWidth(),
        widget.getHeight(),
        chart.getChartType(),
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
