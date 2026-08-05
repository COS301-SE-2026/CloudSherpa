package com.cloudsherpa.ingestion.provider.mock.registry;

import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import java.util.Map;

public class ImmutableMockMetricRegistry implements MockMetricRegistry {

  private final Map<String, MockServiceDefinition> services;

  public ImmutableMockMetricRegistry(Map<String, MockServiceDefinition> services) {

    this.services = Map.copyOf(services);
  }

  @Override
  public MockServiceDefinition service(String serviceName) {

    MockServiceDefinition definition = services.get(serviceName);

    if (definition == null) {
      throw new IllegalArgumentException(
          "No mock definition registered for service " + serviceName);
    }

    return definition;
  }
}
