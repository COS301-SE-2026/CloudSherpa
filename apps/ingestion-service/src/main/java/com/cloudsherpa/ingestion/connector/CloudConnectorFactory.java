package com.cloudsherpa.ingestion.connector;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CloudConnectorFactory {

  private final Map<String, CloudConnector> connectors;

  public CloudConnectorFactory(List<CloudConnector> connectorList) {
    this.connectors = connectorList.stream()
        .collect(Collectors.toMap(
            c -> c.getProviderName().toLowerCase(),
            Function.identity()));
  }

  public CloudConnector getConnector(String provider) {
    return connectors.get(provider.toLowerCase());
  }
}
