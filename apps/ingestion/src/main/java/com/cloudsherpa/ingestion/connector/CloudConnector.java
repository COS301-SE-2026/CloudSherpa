package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;

public interface CloudConnector {
  String getProviderName();

  List<String> getAllOfferedServices();

  List<ResourceDetail> getAllResources(CloudCredentials credentials, List<String> serviceTypes);

  boolean testConnection(CloudCredentials credentials);
}
