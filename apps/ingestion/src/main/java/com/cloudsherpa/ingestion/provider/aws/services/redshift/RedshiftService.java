package com.cloudsherpa.ingestion.provider.aws.services.redshift;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalCluster;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.redshift.model.Cluster;

public interface RedshiftService {
  public List<RegionalCluster> getAllRedshiftClusters(CloudCredentials credentials);

  public Map<String, String> getTagsForCluster(Cluster cluster);

  public List<ResourceDetail> getAllRedshiftClustersWithTags(CloudCredentials credentials);
}
