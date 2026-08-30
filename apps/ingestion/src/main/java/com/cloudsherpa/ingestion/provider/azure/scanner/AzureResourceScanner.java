package com.cloudsherpa.ingestion.provider.azure.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Set;

public interface AzureResourceScanner {

  String getServiceName();

  List<String> getResourceTypes();

  Set<String> getPermissionsRequired();

  ResourceDetail scan(JsonNode resource, CloudCredentials credentials);
}
