package com.cloudsherpa.ingestion.provider.mock.simulation;

import com.cloudsherpa.ingestion.provider.mock.definition.SimulationProfile;

public final class MetricSimulationContext {

  private double state;

  private final double gaussian;

  private final double clusterFactor;

  private final double burst;

  private final SimulationProfile profile;

  public MetricSimulationContext(
      double state,
      double gaussian,
      double clusterFactor,
      double burst,
      SimulationProfile profile) {

    this.state = state;
    this.gaussian = gaussian;
    this.clusterFactor = clusterFactor;
    this.burst = burst;
    this.profile = profile;
  }

  public double state() {
    return state;
  }

  public void state(double state) {
    this.state = state;
  }

  public double gaussian() {
    return gaussian;
  }

  public double clusterFactor() {
    return clusterFactor;
  }

  public double burst() {
    return burst;
  }

  public SimulationProfile profile() {
    return profile;
  }
}
