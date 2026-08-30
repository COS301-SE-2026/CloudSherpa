package com.cloudsherpa.ingestion.provider.mock.definition;

/**
 * Common simulation profiles shared between cloud providers. These are provider-agnostic. AWS, GCP
 * and Azure can all reuse them.
 */
public final class Profiles {

  private Profiles() {}

  public static final SimulationProfile CPU =
      new SimulationProfile(0.8, 1.0, 0.0, 0.0, 100.0, 2.0, true);

  /** Generic percentage metric. */
  public static final SimulationProfile PERCENTAGE =
      new SimulationProfile(0.6, 1.0, 0.0, 0.0, 100.0, 1.5, true);

  /** Bytes transferred. */
  public static final SimulationProfile THROUGHPUT =
      new SimulationProfile(1.0, 1000.0, 0.0, 0.0, Double.MAX_VALUE, 25.0, false);

  /** Generic latency metric. */
  public static final SimulationProfile LATENCY =
      new SimulationProfile(0.5, 3.0, 10.0, 0.0, Double.MAX_VALUE, 1.0, false);

  /** Disk I/O operations. */
  public static final SimulationProfile IOPS =
      new SimulationProfile(0.8, 25.0, 0.0, 0.0, Double.MAX_VALUE, 3.0, false);

  /** Generic count. */
  public static final SimulationProfile COUNT =
      new SimulationProfile(0.2, 1.0, 0.0, 0.0, Double.MAX_VALUE, 0.5, false);

  /** Binary event (0/1). */
  public static final SimulationProfile BOOLEAN_EVENT =
      new SimulationProfile(1.0, 1.0, 0.0, 0.0, 1.0, 0.0, true);

  /** Loss/error metrics. */
  public static final SimulationProfile LOSS =
      new SimulationProfile(0.4, -0.01, 1.0, 0.0, 1.0, 0.2, true);

  /** Timing metrics. */
  public static final SimulationProfile DURATION =
      new SimulationProfile(0.7, 5.0, 100.0, 0.0, Double.MAX_VALUE, 4.0, false);

  /** Memory utilisation. */
  public static final SimulationProfile MEMORY =
      new SimulationProfile(0.7, 1.0, 0.0, 0.0, 100.0, 2.0, true);

  /** GPU utilisation. */
  public static final SimulationProfile GPU =
      new SimulationProfile(0.9, 1.0, 0.0, 0.0, 100.0, 2.5, true);

  /** Storage capacity metrics. */
  public static final SimulationProfile STORAGE =
      new SimulationProfile(0.3, 2048.0, 0.0, 0.0, Double.MAX_VALUE, 15.0, false);
}
