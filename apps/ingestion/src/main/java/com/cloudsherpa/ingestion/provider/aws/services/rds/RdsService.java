package com.cloudsherpa.ingestion.provider.aws.services.rds;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;
import software.amazon.awssdk.services.rds.model.DBInstance;

public interface RdsService {
  public List<DBInstance> getAllRdsInstances(CloudCredentials credentials);

  public List<ResourceDetail> getAllRdsInstancesWithTags(CloudCredentials credentials);
}
