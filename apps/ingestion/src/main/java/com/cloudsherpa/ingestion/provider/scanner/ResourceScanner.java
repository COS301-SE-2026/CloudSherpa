package com.cloudsherpa.ingestion.provider.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;
import java.util.Set;

public interface ResourceScanner {

  String getProvider();

  String getServiceName();

  Set<String> getPermissionsRequired();

  List<ResourceDetail> scan(CloudCredentials credentials);
}
