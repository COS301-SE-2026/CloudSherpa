package com.cloudsherpa.ingestion.provider.mock.definition;

import com.cloudsherpa.ingestion.provider.mock.simulation.MetricSimulator;

public final class MetricDefinition {

  private final String name;
  private final String unit;
  private final SimulationProfile profile;
  private final MetricSimulator simulator;

  public MetricDefinition(
      String name, String unit, SimulationProfile profile, MetricSimulator simulator) {

    this.name = name;
    this.unit = unit;
    this.profile = profile;
    this.simulator = simulator;
  }

  public String name() {
    return name;
  }

  public String unit() {
    return unit;
  }

  public SimulationProfile profile() {
    return profile;
  }

  public MetricSimulator simulator() {
    return simulator;
  }
}
