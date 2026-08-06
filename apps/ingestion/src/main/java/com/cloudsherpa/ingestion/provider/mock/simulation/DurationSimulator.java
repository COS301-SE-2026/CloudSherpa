package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class DurationSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    double value = context.profile().offset() + context.state() * context.profile().scale();

    value += context.burst() * context.profile().burstWeight();
    value += context.gaussian() * context.profile().noiseMultiplier();

    return Math.max(context.profile().minimum(), value);
  }
}
