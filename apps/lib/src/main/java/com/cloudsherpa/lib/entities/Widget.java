package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "widget", schema = "public")
public class Widget {

  @Id
  @Column(name = "widget_id", nullable = false)
  private UUID id;

  @Column(name = "dashboard_id", nullable = false)
  private UUID dashboardId;

  @ManyToOne
  @JoinColumn(name = "dashboard_id", nullable = false, insertable = false, updatable = false)
  private Dashboard dashboard;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type", nullable = false, columnDefinition = "public.type_enum")
  private TypeEnum type;

  @Column(name = "start_x", nullable = false)
  private Integer startX;

  @Column(name = "start_y", nullable = false)
  private Integer startY;

  @Column(name = "width", nullable = false)
  private Integer width;

  @Column(name = "height", nullable = false)
  private Integer height;

  @Column(name = "display_name", length = 100)
  private String displayName;

  protected Widget() {}

  public Widget(
      UUID id,
      UUID dashboardId,
      TypeEnum type,
      Integer startX,
      Integer startY,
      Integer width,
      Integer height,
      String displayName) {
    this.id = id;
    this.dashboardId = dashboardId;
    this.type = type;
    this.startX = startX;
    this.startY = startY;
    this.width = width;
    this.height = height;
    this.displayName = displayName;
  }

  public UUID getId() {
    return id;
  }

  public UUID getDashboardId() {
    return dashboardId;
  }

  public Dashboard getDashboard() {
    return dashboard;
  }

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public Integer getStartX() {
    return startX;
  }

  public void setStartX(Integer startX) {
    this.startX = startX;
  }

  public Integer getStartY() {
    return startY;
  }

  public void setStartY(Integer startY) {
    this.startY = startY;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
