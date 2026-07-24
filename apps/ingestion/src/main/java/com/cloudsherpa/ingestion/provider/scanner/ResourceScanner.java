package com.cloudsherpa.ingestion.provider.scanner;

import java.util.List;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;

public interface ResourceScanner {

  String getProvider();

  String getServiceName();

  List<ResourceDetail> scan(CloudCredentials credentials);
}
