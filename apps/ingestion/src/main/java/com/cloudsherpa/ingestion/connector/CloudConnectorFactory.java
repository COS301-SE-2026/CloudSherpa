package com.cloudsherpa.ingestion.connector;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CloudConnectorFactory {

  private final Map<String, CloudConnector> connectors;

  public CloudConnectorFactory(Map<String, CloudConnector> connectors) {
    this.connectors = connectors;
  }

  public CloudConnector getConnector(String provider) {

    CloudConnector connector = connectors.get(provider.toUpperCase());

    if (connector == null) {
      throw new IllegalArgumentException("No connector found for provider: " + provider);
    }

    return connector;
  }
}
