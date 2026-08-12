package com.cloudsherpa.ingestion.provider.mock.definition;

import java.util.Objects;

public record SimulationProfile(
    double burstWeight,
    double scale,
    double offset,
    double minimum,
    double maximum,
    double noiseMultiplier,
    boolean clamp) {

  public SimulationProfile {

    if (maximum < minimum) {
      throw new IllegalArgumentException("Maximum value cannot be less than minimum value.");
    }

    if (noiseMultiplier < 0) {
      throw new IllegalArgumentException("Noise multiplier cannot be negative.");
    }
  }

  /** Applies clamping if enabled. */
  public double applyBounds(double value) {

    if (!clamp) {
      return value;
    }
    return Math.clamp(value, minimum, maximum);
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
