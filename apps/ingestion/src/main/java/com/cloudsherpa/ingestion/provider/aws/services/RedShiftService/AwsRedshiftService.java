package com.cloudsherpa.ingestion.provider.aws.services.RedShiftService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.AwsClientFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.Tag;

public class AwsRedshiftService implements RedshiftService {
  @Override
  public List<Cluster> getAllRedshiftClusters(CloudCredentials credentials) {
    List<Cluster> clusters = new ArrayList<>();

    try (RedshiftClient client =
        RedshiftClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      DescribeClustersResponse response = client.describeClusters();
      clusters = response.clusters();
    }
    return clusters;
  }

  @Override
  public Map<String, String> getTagsForCluster(Cluster cluster) {
    return cluster.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
  }

  @Override
  public List<ResourceDetail> getAllRedshiftClustersWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();
    for (Cluster cluster : getAllRedshiftClusters(credentials)) {

      Map<String, String> tags = getTagsForCluster(cluster);
      String name =
          ResourceDetail.resolveName(
              cluster.clusterIdentifier(), cluster.clusterIdentifier(), tags);
      resources.add(
          new ResourceDetail(
              cluster.clusterIdentifier(), name, "ClusterIdentifier", "REDSHIFT", tags));
    }
    return resources;
  }
}
