package com.cloudsherpa.ingestion.connector;

import java.util.List;

public class ServiceScope {
  private String name;
  private List<InstanceScope> instances;
  private List<Metric> metrics;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<InstanceScope> getInstances() {
    return this.instances;
  }

  public void setInstances(List<InstanceScope> instances) {
    this.instances = instances;
  }

  public List<Metric> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<Metric> metrics) {
    this.metrics = metrics;
  }
}
