package com.cloudsherpa.ingestion.provider.mock.registry;

import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ImmutableMockMetricRegistry implements MockMetricRegistry {
  private final Map<String, MockServiceDefinition> services;

  public ImmutableMockMetricRegistry(Map<String, MockServiceDefinition> services) {

    Objects.requireNonNull(services, "Registered services cannot be null.");

    this.services = Collections.unmodifiableMap(new LinkedHashMap<>(services));
  }

  @Override
  public MockServiceDefinition service(String serviceName) {
    MockServiceDefinition definition = services.get(serviceName);

    if (definition == null) {
      throw new IllegalArgumentException("No mock service registered for: " + serviceName);
    }

    return definition;
  }

  @Override
  public boolean contains(String serviceName) {
    return services.containsKey(serviceName);
  }

  @Override
  public Collection<MockServiceDefinition> services() {
    return services.values();
  }
}
