package com.cloudsherpa.ingestion.provider.gcp.services.cloudrun;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.google.cloud.asset.v1.ResourceSearchResult;

public interface CloudRunService {

  ResourceDetail getResourceDetail(ResourceSearchResult resource, CloudCredentials credentials);
}
