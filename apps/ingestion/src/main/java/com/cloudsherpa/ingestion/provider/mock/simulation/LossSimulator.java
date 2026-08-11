package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class LossSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    return 1.0 / (1.0 + Math.max(0.1, context.state()));
  }
}
