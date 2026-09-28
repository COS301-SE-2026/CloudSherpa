package com.cloudsherpa.service.agenticdashboard.validation;

import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DashboardPlanValidator {

  private static final int MAX_WIDGETS = 20;
  private static final int MAX_GRID_WIDTH = 12;
  private static final int MAX_WIDGET_HEIGHT = 1000;

  public void validate(DashboardPlanDto plan) {
    if (plan == null) {
      throw invalid("Dashboard plan is required");
    }

    validateDashboard(plan);
    validateWidgets(plan);
  }

  private void validateDashboard(DashboardPlanDto plan) {
    if (plan.title() == null || plan.title().isBlank()) {
      throw invalid("Dashboard title is required");
    }

    if (plan.title().length() > 80) {
      throw invalid("Dashboard title must not exceed 80 characters");
    }

    if (plan.description() != null && plan.description().length() > 2000) {
      throw invalid("Dashboard description must not exceed 2000 characters");
    }

    if (plan.timeFrom() != null
        && plan.timeTo() != null
        && plan.timeFrom().isAfter(plan.timeTo())) {
      throw invalid("Dashboard start time must be before end time");
    }

    if (plan.widgets() == null) {
      throw invalid("Dashboard widgets are required");
    }

    if (plan.widgets().size() > MAX_WIDGETS) {
      throw invalid("Dashboard cannot contain more than " + MAX_WIDGETS + " widgets");
    }
  }

  private void validateWidgets(DashboardPlanDto plan) {
    Set<java.util.UUID> widgetIds = new HashSet<>();

    for (DashboardPlanWidgetDto widget : plan.widgets()) {
      validateWidget(widget);

      if (!widgetIds.add(widget.widgetId())) {
        throw invalid("Widget IDs must be unique");
      }
    }
  }

  private void validateWidget(DashboardPlanWidgetDto widget) {
    if (widget == null) {
      throw invalid("Widget cannot be null");
    }

    if (widget.widgetId() == null) {
      throw invalid("Widget ID is required");
    }

    if (widget.widgetType() == null) {
      throw invalid("Widget type is required");
    }

    if (widget.displayName() == null || widget.displayName().isBlank()) {
      throw invalid("Widget display name is required");
    }

    if (widget.displayName().length() > 80) {
      throw invalid("Widget display name must not exceed 80 characters");
    }

    if (widget.startX() == null || widget.startX() < 0) {
      throw invalid("Widget startX must be zero or greater");
    }

    if (widget.startY() == null || widget.startY() < 0) {
      throw invalid("Widget startY must be zero or greater");
    }

    if (widget.width() == null || widget.width() <= 0 || widget.width() > MAX_GRID_WIDTH) {
      throw invalid("Widget width must be between 1 and 12");
    }

    if (widget.height() == null || widget.height() <= 0 || widget.height() > MAX_WIDGET_HEIGHT) {
      throw invalid("Widget height must be between 1 and " + MAX_WIDGET_HEIGHT);
    }

    if (widget.startX() + widget.width() > MAX_GRID_WIDTH) {
      throw invalid("Widget must fit within the 12-column dashboard grid");
    }

    if (widget.widgetType() == TypeEnum.CHART) {
      validateChartWidget(widget);
    }

    if (widget.widgetType() == TypeEnum.KPI) {
      validateKpiWidget(widget);
    }
  }

  private void validateChartWidget(DashboardPlanWidgetDto widget) {
    if (widget.chartType() == null) {
      throw invalid("Chart type is required");
    }

    if (widget.provider() == null) {
      throw invalid("Chart provider is required");
    }

    if (widget.accountId() == null) {
      throw invalid("Chart account ID is required");
    }

    if (widget.resourceId() == null) {
      throw invalid("Chart resource ID is required");
    }

    if (widget.metricType() == null || widget.metricType().isBlank()) {
      throw invalid("Chart metric type is required");
    }

    if (widget.metricType().length() > 50) {
      throw invalid("Chart metric type must not exceed 50 characters");
    }

    if (widget.metricName() == null || widget.metricName().isBlank()) {
      throw invalid("Chart metric name is required");
    }
  }

  private void validateKpiWidget(DashboardPlanWidgetDto widget) {
    if (widget.chargeIds() == null || widget.chargeIds().isEmpty()) {
      throw invalid("KPI charge IDs are required");
    }

    if (widget.aggregationWindowDays() == null || widget.aggregationWindowDays() <= 0) {
      throw invalid("KPI aggregation window must be greater than zero");
    }
  }

  private ResponseStatusException invalid(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
