package com.cloudsherpa.ingestion.provider.mock.definition;

import java.util.Objects;

public final class SimulationProfile {
  private final double burstWeight;
  private final double scale;
  private final double offset;
  private final double minimum;
  private final double maximum;
  private final double noiseMultiplier;
  private final boolean clamp;

  public SimulationProfile(
      double burstWeight,
      double scale,
      double offset,
      double minimum,
      double maximum,
      double noiseMultiplier,
      boolean clamp) {

    if (maximum < minimum) {
      throw new IllegalArgumentException("Maximum value cannot be less than minimum value.");
    }

    if (noiseMultiplier < 0) {
      throw new IllegalArgumentException("Noise multiplier cannot be negative.");
    }

    this.burstWeight = burstWeight;
    this.scale = scale;
    this.offset = offset;
    this.minimum = minimum;
    this.maximum = maximum;
    this.noiseMultiplier = noiseMultiplier;
    this.clamp = clamp;
  }

  public double burstWeight() {
    return burstWeight;
  }

  public double scale() {
    return scale;
  }

  public double offset() {
    return offset;
  }

  public double minimum() {
    return minimum;
  }

  public double maximum() {
    return maximum;
  }

  public double noiseMultiplier() {
    return noiseMultiplier;
  }

  public boolean clamp() {
    return clamp;
  }

  /** Applies clamping if enabled. */
  public double applyBounds(double value) {

    if (!clamp) {
      return value;
    }

    return Math.max(minimum, Math.min(maximum, value));
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof SimulationProfile other)) {
      return false;
    }

    return Double.compare(other.burstWeight, burstWeight) == 0
        && Double.compare(other.scale, scale) == 0
        && Double.compare(other.offset, offset) == 0
        && Double.compare(other.minimum, minimum) == 0
        && Double.compare(other.maximum, maximum) == 0
        && Double.compare(other.noiseMultiplier, noiseMultiplier) == 0
        && clamp == other.clamp;
  }

  @Override
  public int hashCode() {

    return Objects.hash(burstWeight, scale, offset, minimum, maximum, noiseMultiplier, clamp);
  }

  @Override
  public String toString() {

    return "SimulationProfile{"
        + "burstWeight="
        + burstWeight
        + ", scale="
        + scale
        + ", offset="
        + offset
        + ", minimum="
        + minimum
        + ", maximum="
        + maximum
        + ", noiseMultiplier="
        + noiseMultiplier
        + ", clamp="
        + clamp
        + '}';
  }
}
