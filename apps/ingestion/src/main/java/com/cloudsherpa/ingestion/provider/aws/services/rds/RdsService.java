package com.cloudsherpa.ingestion.provider.aws.services.rds;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDbInstance;
import java.util.List;

public interface RdsService {
  public List<RegionalDbInstance> getAllRdsInstances(CloudCredentials credentials);

  public List<ResourceDetail> getAllRdsInstancesWithTags(CloudCredentials credentials);
}
