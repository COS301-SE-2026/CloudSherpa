package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class LatencySimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    double value = context.profile().offset() + context.state() * context.profile().scale();

    value += context.clusterFactor() * 20;

    value += context.burst();

    value += context.gaussian() * context.profile().noiseMultiplier();

    return Math.max(context.profile().minimum(), value);
  }
}
