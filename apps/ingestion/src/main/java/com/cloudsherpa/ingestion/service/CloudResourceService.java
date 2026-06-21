package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.permissions.AwsPermissionsBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Intermediary between the CloudResourceController and the ingestion pipeline. */
@Service
public class CloudResourceService {
  private final CloudConnectorFactory factory;

  public CloudResourceService(CloudConnectorFactory factory) {
    this.factory = factory;
  }

  public List<String> getAllOfferedServices(String provider) {
    List<String> services = new ArrayList<>();
    CloudConnector connector = factory.getConnector(provider);
    services.addAll(connector.getAllOfferedServices());
    return services;
  }

  public List<ResourceDetail> getAllResources(String provider, CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();
    CloudConnector connector = factory.getConnector(provider);
    resources.addAll(connector.getAllResources(credentials));
    return resources;
  }

  public String generateAwsPermissionsPolicy(List<String> services) {
    return AwsPermissionsBuilder.buildPolicy(services);
  }
}
