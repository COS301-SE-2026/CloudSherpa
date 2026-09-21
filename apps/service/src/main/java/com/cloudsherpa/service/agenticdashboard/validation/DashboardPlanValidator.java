package com.cloudsherpa.service.agenticdashboard.validation;

import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
/*
 * This validator only validates the general field requirements of the widgets.
 * The specific AI tool layer is going to
 * validate the resources and metrics against the session user's specific
 * accounts.
 */
public class DashboardPlanValidator {

  private static final int MAX_WIDGETS = 20;
  private static final int MAX_GRID_WIDTH = 12;

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
    for (DashboardPlanWidgetDto widget : plan.widgets()) {
      validateWidget(widget);
    }
  }

  private void validateWidget(DashboardPlanWidgetDto widget) {
    if (widget == null) {
      throw invalid("Widget cannot be null");
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

    if (widget.height() == null || widget.height() <= 0) {
      throw invalid("Widget height must be greater than zero");
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
