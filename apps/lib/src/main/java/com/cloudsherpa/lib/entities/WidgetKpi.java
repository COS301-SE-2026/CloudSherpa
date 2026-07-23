package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "widget_kpi", schema = "public")
public class WidgetKpi {

    @Id
    @Column(name = "kpi_id", nullable = false)
    private UUID id;

    @Column(name = "widget_id", nullable = false)
    private UUID widgetId;

    @ManyToOne
    @JoinColumn(name = "widget_id", nullable = false, insertable = false, updatable = false)
    private Widget widget;

    @Column(name = "aggregation", nullable = false)
    private Integer aggregation;

    protected WidgetKpi() {}

    public WidgetKpi(UUID id, UUID widgetId, Integer aggregation) {
        this.id = id;
        this.widgetId = widgetId;
        this.aggregation = aggregation;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWidgetId() {
        return widgetId;
    }

    public Widget getWidget() {
        return widget;
    }

    public Integer getAggregation() {
        return aggregation;
    }

    public void setAggregation(Integer aggregation) {
        this.aggregation = aggregation;
    }
}