package com.cloudsherpa.lib.entities;

import com.cloudsherpa.lib.entities.ProviderEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
    name = "offered_metric",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_offered_metric",
            columnNames = {
                "provider",
                "service_type",
                "metric_name"
            })
    })
public class OfferedMetric {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "offered_metric_id", nullable = false, updatable = false)
  private UUID offeredMetricId;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false)
  private ProviderEnum provider;

  @Column(name = "service_type", nullable = false, length = 255)
  private String serviceType;

  @Column(name = "metric_name", nullable = false, length = 255)
  private String metricName;

  @Column(name = "identifier_field", nullable = false, length = 100)
  private String identifierField;

  @Column(name = "expected_unit", length = 50)
  private String expectedUnit;

  @Column(name = "description")
  private String description;

  public OfferedMetric() {}

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final OfferedMetric offeredMetric = new OfferedMetric();

    public Builder offeredMetricId(UUID offeredMetricId) {
        offeredMetric.setOfferedMetricId(offeredMetricId);
        return this;
    }

    public Builder provider(ProviderEnum provider) {
        offeredMetric.setProvider(provider);
        return this;
    }

    public Builder serviceType(String serviceType) {
        offeredMetric.setServiceType(serviceType);
        return this;
    }

    public Builder metricName(String metricName) {
        offeredMetric.setMetricName(metricName);
        return this;
    }

    public Builder identifierField(String identifierField) {
        offeredMetric.setIdentifierField(identifierField);
        return this;
    }

    public Builder expectedUnit(String expectedUnit) {
        offeredMetric.setExpectedUnit(expectedUnit);
        return this;
    }

    public Builder description(String description) {
        offeredMetric.setDescription(description);
        return this;
    }

    public OfferedMetric build() {
        return offeredMetric;
    }
  }
  public UUID getOfferedMetricId() {
    return offeredMetricId;
  }

  public void setOfferedMetricId(UUID offeredMetricId) {
    this.offeredMetricId = offeredMetricId;
  }

  public ProviderEnum getProvider() {
    return provider;
  }

  public void setProvider(ProviderEnum provider) {
    this.provider = provider;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getIdentifierField() {
    return identifierField;
  }

  public void setIdentifierField(String identifierField) {
    this.identifierField = identifierField;
  }

  public String getExpectedUnit() {
    return expectedUnit;
  }

  public void setExpectedUnit(String expectedUnit) {
    this.expectedUnit = expectedUnit;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
