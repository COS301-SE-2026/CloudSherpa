package com.cloudsherpa.ingestion.provider.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;

public interface ResourceScanner {

  String getProvider();

  String getServiceName();

  List<ResourceDetail> scan(CloudCredentials credentials);
}
