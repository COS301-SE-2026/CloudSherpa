package com.cloudsherpa.ingestion.provider.azure.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface AzureResourceScanner {

  String getServiceName();

  List<String> getResourceTypes();

  ResourceDetail scan(JsonNode resource, CloudCredentials credentials);
}
