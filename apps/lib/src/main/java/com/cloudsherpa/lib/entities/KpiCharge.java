package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "kpi_charges", schema = "public")
public class KpiCharge {

  @Id
  @Column(name = "kpi_charges_id", nullable = false)
  private UUID id;

  @Column(name = "widget_kpi_id")
  private UUID widgetKpiId;

  @ManyToOne
  @JoinColumn(name = "widget_kpi_id", insertable = false, updatable = false)
  private WidgetKpi widgetKpi;

  @Column(name = "charge_id", length = 2128)
  private String chargeId;

  protected KpiCharge() {}

  public KpiCharge(UUID id, UUID widgetKpiId, String chargeId) {
    this.id = id;
    this.widgetKpiId = widgetKpiId;
    this.chargeId = chargeId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getWidgetKpiId() {
    return widgetKpiId;
  }

  public WidgetKpi getWidgetKpi() {
    return widgetKpi;
  }

  public String getChargeId() {
    return chargeId;
  }
}