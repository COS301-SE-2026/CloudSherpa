package com.cloudsherpa.ingestion.provider.mock.factory;

import com.cloudsherpa.ingestion.provider.mock.definition.MetricDefinition;
import com.cloudsherpa.ingestion.provider.mock.definition.Profiles;
import com.cloudsherpa.ingestion.provider.mock.simulation.BooleanEventSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.CounterSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.DurationSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.LatencySimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.LossSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.PercentageSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.StorageSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.ThroughputSimulator;
import org.springframework.stereotype.Component;

/**
 * Factory responsible for creating immutable MetricDefinitions.
 *
 * <p>This class intentionally contains no provider-specific knowledge. AWS, GCP and Azure simply
 * map their metrics onto one of the common simulation behaviours exposed here.
 */
@Component
public class MetricDefinitionFactory {

  private final PercentageSimulator percentageSimulator;

  private final ThroughputSimulator throughputSimulator;

  private final CounterSimulator counterSimulator;

  private final LatencySimulator latencySimulator;

  private final BooleanEventSimulator booleanEventSimulator;

  private final LossSimulator lossSimulator;

  private final DurationSimulator durationSimulator;

  private final StorageSimulator storageSimulator;

  private static final String PERCENT = "Percent";

  public MetricDefinitionFactory(
      PercentageSimulator percentageSimulator,
      ThroughputSimulator throughputSimulator,
      CounterSimulator counterSimulator,
      LatencySimulator latencySimulator,
      BooleanEventSimulator booleanEventSimulator,
      LossSimulator lossSimulator,
      DurationSimulator durationSimulator,
      StorageSimulator storageSimulator) {

    this.percentageSimulator = percentageSimulator;
    this.throughputSimulator = throughputSimulator;
    this.counterSimulator = counterSimulator;
    this.latencySimulator = latencySimulator;
    this.booleanEventSimulator = booleanEventSimulator;
    this.lossSimulator = lossSimulator;
    this.durationSimulator = durationSimulator;
    this.storageSimulator = storageSimulator;
  }

  public MetricDefinition percentage(String metricName, String unit) {

    return new MetricDefinition(metricName, unit, Profiles.PERCENTAGE, percentageSimulator);
  }

  public MetricDefinition cpu(String metricName) {

    return new MetricDefinition(metricName, PERCENT, Profiles.CPU, percentageSimulator);
  }

  public MetricDefinition cpu(String metricName, String metricUnit) {

    return new MetricDefinition(metricName, metricUnit, Profiles.CPU, percentageSimulator);
  }

  public MetricDefinition memory(String metricName) {

    return new MetricDefinition(metricName, PERCENT, Profiles.MEMORY, percentageSimulator);
  }

  public MetricDefinition gpu(String metricName) {

    return new MetricDefinition(metricName, PERCENT, Profiles.GPU, percentageSimulator);
  }

  public MetricDefinition gpu(String metricName, String metricUnit) {

    return new MetricDefinition(metricName, metricUnit, Profiles.GPU, percentageSimulator);
  }

  public MetricDefinition throughput(String metricName, String unit) {

    return new MetricDefinition(metricName, unit, Profiles.THROUGHPUT, throughputSimulator);
  }

  public MetricDefinition storage(String metricName, String unit) {

    return new MetricDefinition(metricName, unit, Profiles.STORAGE, storageSimulator);
  }

  public MetricDefinition latency(String metricName, String unit) {

    return new MetricDefinition(metricName, unit, Profiles.LATENCY, latencySimulator);
  }

  public MetricDefinition duration(String metricName, String unit) {

    return new MetricDefinition(metricName, unit, Profiles.DURATION, durationSimulator);
  }

  public MetricDefinition counter(String metricName, String unit) {

    return new MetricDefinition(metricName, unit, Profiles.COUNT, counterSimulator);
  }

  public MetricDefinition booleanEvent(String metricName) {

    return new MetricDefinition(metricName, "Count", Profiles.BOOLEAN_EVENT, booleanEventSimulator);
  }

  public MetricDefinition loss(String metricName) {

    return new MetricDefinition(metricName, "None", Profiles.LOSS, lossSimulator);
  }
}
