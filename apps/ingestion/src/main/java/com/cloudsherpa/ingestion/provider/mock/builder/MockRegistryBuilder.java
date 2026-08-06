package com.cloudsherpa.ingestion.provider.mock.builder;

import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.registry.ImmutableMockMetricRegistry;
import java.util.LinkedHashMap;
import java.util.Map;

public class MockRegistryBuilder {

  private final Map<String, MockServiceDefinition> services = new LinkedHashMap<>();

  public ServiceDefinitionBuilder service(String serviceName) {

    return new ServiceDefinitionBuilder(this, serviceName);
  }

  void register(MockServiceDefinition service) {

    services.put(service.serviceName(), service);
  }

  public ImmutableMockMetricRegistry build() {

    return new ImmutableMockMetricRegistry(services);
  }
}
