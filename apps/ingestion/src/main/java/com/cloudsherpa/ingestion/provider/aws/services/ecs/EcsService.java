package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import java.util.List;

public interface EcsService {
  public List<RegionalArn> getAllEcsClusterArns(CloudCredentials credentials);

  public List<ResourceDetail> getAllEcsClustersWithTags(CloudCredentials credentials);
}
