package com.cloudsherpa.service.scheduler.dto;

public class Metric {
  private String name;
  private String unit;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }
}
