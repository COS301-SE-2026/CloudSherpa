package com.cloudsherpa.ingestion.provider.mock.registry;

import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import java.util.Collection;

public interface MockMetricRegistry {
  /**
   * Returns the service definition for the supplied provider-specific service name.
   *
   * @throws IllegalArgumentException if the service is not registered.
   */
  MockServiceDefinition service(String serviceName);

  /** Returns true if the registry contains the supplied service. */
  boolean contains(String serviceName);

  /** Returns every registered service. */
  Collection<MockServiceDefinition> services();
}
