package com.cloudsherpa.ingestion.provider.aws.services.EksService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;

public interface EksService {
  public List<String> getAllEksClusterArns(CloudCredentials credentials);

  public List<ResourceDetail> getAllEksClustersWithTags(CloudCredentials credentials);
}
