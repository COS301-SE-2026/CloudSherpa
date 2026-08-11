package com.cloudsherpa.ingestion.provider.mock.simulation;

@FunctionalInterface
public interface MetricSimulator {

  double simulate(MetricSimulationContext context);
}
