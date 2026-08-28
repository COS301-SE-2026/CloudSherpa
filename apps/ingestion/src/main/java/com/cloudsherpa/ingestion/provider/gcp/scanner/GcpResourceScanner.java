package com.cloudsherpa.ingestion.provider.gcp.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;

public interface GcpResourceScanner {

  String getProvider();

  String getServiceName();

  List<String> getAssetTypes();

  Set<String> getPermissionsRequired();

  ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials);
}
