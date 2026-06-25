package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;

public interface EcsService {
  public List<String> getAllEcsClusterArns(CloudCredentials credentials);

  public List<ResourceDetail> getAllEcsClustersWithTags(CloudCredentials credentials);
}
