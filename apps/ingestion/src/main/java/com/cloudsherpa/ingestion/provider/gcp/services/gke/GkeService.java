package com.cloudsherpa.ingestion.provider.gcp.services.gke;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.google.cloud.asset.v1.ResourceSearchResult;

public interface GkeService {

  ResourceDetail getResourceDetail(ResourceSearchResult resource, CloudCredentials credentials);
}
