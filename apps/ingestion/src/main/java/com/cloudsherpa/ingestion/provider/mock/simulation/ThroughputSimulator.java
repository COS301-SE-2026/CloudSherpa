package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class ThroughputSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    double value = context.state() * context.profile().scale() * context.clusterFactor();

    value += context.burst() * context.profile().burstWeight();
    value += context.gaussian() * context.profile().noiseMultiplier();

    return Math.max(context.profile().minimum(), value);
  }
}
