package com.cloudsherpa.ingestion.provider.gcp.services.compute;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.google.cloud.asset.v1.Asset;

public interface ComputeService {

  public ResourceDetail getResourceDetail(Asset asset, CloudCredentials credentials);
}
