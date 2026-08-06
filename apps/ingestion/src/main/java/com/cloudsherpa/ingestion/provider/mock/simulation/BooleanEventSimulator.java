package com.cloudsherpa.ingestion.provider.mock.simulation;

import org.springframework.stereotype.Component;

@Component
public class BooleanEventSimulator implements MetricSimulator {

  @Override
  public double simulate(MetricSimulationContext context) {

    return context.burst() > 25 ? 1 : 0;
  }
}
