package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class CounterSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    double value =
        context.state() * context.profile().scale()
            + context.gaussian() * context.profile().noiseMultiplier();

    value += context.burst();

    return Math.max(context.profile().minimum(), value);
  }
}
