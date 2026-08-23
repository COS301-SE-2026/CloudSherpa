package com.cloudsherpa.ingestion.provider.gcp.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;

public interface GcpResourceScanner {

  String getProvider();

  String getServiceName();

  List<String> getAssetTypes();

  ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials);
}
