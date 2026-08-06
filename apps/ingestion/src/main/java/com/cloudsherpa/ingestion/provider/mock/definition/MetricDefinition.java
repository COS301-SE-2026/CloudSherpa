package com.cloudsherpa.ingestion.provider.mock.definition;

import com.cloudsherpa.ingestion.provider.mock.simulation.MetricSimulator;
import java.util.Objects;

public final class MetricDefinition {
  private final String name;
  private final String unit;
  private final SimulationProfile profile;
  private final MetricSimulator simulator;

  public MetricDefinition(
      String name, String unit, SimulationProfile profile, MetricSimulator simulator) {
    this.name = Objects.requireNonNull(name, "Metric name cannot be null.");
    this.unit = Objects.requireNonNull(unit, "Metric unit cannot be null.");
    this.profile = Objects.requireNonNull(profile, "Simulation profile cannot be null.");
    this.simulator = Objects.requireNonNull(simulator, "Metric simulator cannot be null.");
  }

  /** Metric name exactly as exposed by the provider. */
  public String name() {
    return name;
  }

  /** Metric unit. */
  public String unit() {
    return unit;
  }

  /** Statistical simulation profile. */
  public SimulationProfile profile() {
    return profile;
  }

  /** Simulator responsible for generating values. */
  public MetricSimulator simulator() {
    return simulator;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof MetricDefinition other)) {
      return false;
    }

    return name.equals(other.name)
        && unit.equals(other.unit)
        && profile.equals(other.profile)
        && simulator.equals(other.simulator);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, unit, profile, simulator);
  }

  @Override
  public String toString() {

    return "MetricDefinition{" + "name='" + name + '\'' + ", unit='" + unit + '\'' + '}';
  }
}
