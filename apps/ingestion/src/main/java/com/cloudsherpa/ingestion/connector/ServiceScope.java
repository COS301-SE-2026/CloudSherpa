package com.cloudsherpa.ingestion.connector;

import java.util.List;

public class ServiceScope {
  private String name;

  private List<String> metrics;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<String> metrics) {
    this.metrics = metrics;
  }
}
