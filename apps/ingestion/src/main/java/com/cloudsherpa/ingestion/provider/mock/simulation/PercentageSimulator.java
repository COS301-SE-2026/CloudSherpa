package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class PercentageSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    double value =
        context.state()
            + context.gaussian() * context.profile().noiseMultiplier()
            + context.burst() * context.profile().burstWeight();

    value = value * context.profile().scale() + context.profile().offset();

    return context.profile().applyBounds(value);
  }
}
