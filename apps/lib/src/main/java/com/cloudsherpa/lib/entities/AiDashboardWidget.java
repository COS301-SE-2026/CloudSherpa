package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_dashboard_widget", schema = "public")
public class AiDashboardWidget {

  @Id
  @Column(name = "widget_id")
  private UUID widgetId;

  @Column(name = "dashboard_version_id", nullable = false)
  private UUID dashboardVersionId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type", nullable = false, columnDefinition = "public.type_enum")
  private TypeEnum widgetType;

  @Column(name = "start_x", nullable = false)
  private Integer startX;

  @Column(name = "start_y", nullable = false)
  private Integer startY;

  @Column(name = "width", nullable = false)
  private Integer width;

  @Column(name = "height", nullable = false)
  private Integer height;

  @Column(name = "display_name", length = 80)
  private String displayName;

  protected AiDashboardWidget() {
  }

  private AiDashboardWidget(Builder builder) {
    this.widgetId = builder.widgetId;
    this.dashboardVersionId = builder.dashboardVersionId;
    this.widgetType = builder.widgetType;
    this.startX = builder.startX;
    this.startY = builder.startY;
    this.width = builder.width;
    this.height = builder.height;
    this.displayName = builder.displayName;
  }

  public UUID getWidgetId() {
    return widgetId;
  }

  public UUID getDashboardVersionId() {
    return dashboardVersionId;
  }

  public TypeEnum getWidgetType() {
    return widgetType;
  }

  public Integer getStartX() {
    return startX;
  }

  public Integer getStartY() {
    return startY;
  }

  public Integer getWidth() {
    return width;
  }

  public Integer getHeight() {
    return height;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID widgetId;
    private UUID dashboardVersionId;
    private TypeEnum widgetType;
    private Integer startX;
    private Integer startY;
    private Integer width;
    private Integer height;
    private String displayName;

    public Builder widgetId(UUID widgetId) {
      this.widgetId = widgetId;
      return this;
    }

    public Builder dashboardVersionId(UUID dashboardVersionId) {
      this.dashboardVersionId = dashboardVersionId;
      return this;
    }

    public Builder widgetType(TypeEnum widgetType) {
      this.widgetType = widgetType;
      return this;
    }

    public Builder startX(Integer startX) {
      this.startX = startX;
      return this;
    }

    public Builder startY(Integer startY) {
      this.startY = startY;
      return this;
    }

    public Builder width(Integer width) {
      this.width = width;
      return this;
    }

    public Builder height(Integer height) {
      this.height = height;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public AiDashboardWidget build() {
      return new AiDashboardWidget(this);
    }
  }
}
