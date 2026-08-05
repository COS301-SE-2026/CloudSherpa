package com.cloudsherpa.ingestion.provider.mock.registry;

import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;

public interface MockMetricRegistry {

  MockServiceDefinition service(String serviceName);
}
