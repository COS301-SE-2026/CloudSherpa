package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.permissions.AwsPermissionsBuilder;
import com.cloudsherpa.ingestion.provider.gcp.permissions.GcpPermissionsRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  public List<ResourceDetail> getAllResources(
      String provider, CloudCredentials credentials, List<String> services) {
    List<ResourceDetail> resources = new ArrayList<>();
    CloudConnector connector = factory.getConnector(provider);
    resources.addAll(connector.getAllResources(credentials, services));
    return resources;
  }

  public String generateAwsPermissionsPolicy(List<String> services) {
    return AwsPermissionsBuilder.buildPolicy(services);
  }

  public Set<String> generateGcpPermissionsList(List<String> services) {
    return GcpPermissionsRegistry.getPermissions(services.stream().collect(Collectors.toSet()));
  }
}
