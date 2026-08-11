package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class StorageSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    double value = context.state() * context.profile().scale() * context.clusterFactor();

    value += context.gaussian() * context.profile().noiseMultiplier();

    return Math.max(context.profile().minimum(), value);
  }
}
