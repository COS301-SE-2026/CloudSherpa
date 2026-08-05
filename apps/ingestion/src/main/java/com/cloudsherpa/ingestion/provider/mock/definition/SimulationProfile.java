package com.cloudsherpa.ingestion.provider.mock.definition;

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
}
