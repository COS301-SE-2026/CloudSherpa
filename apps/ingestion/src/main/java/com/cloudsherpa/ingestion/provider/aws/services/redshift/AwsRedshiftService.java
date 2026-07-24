package com.cloudsherpa.ingestion.provider.aws.services.redshift;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalCluster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.Tag;

public class AwsRedshiftService implements RedshiftService {
  @Override
  public List<RegionalCluster> getAllRedshiftClusters(CloudCredentials credentials) {
    List<RegionalCluster> regionalClusters = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (RedshiftClient client =
          RedshiftClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        DescribeClustersResponse response = client.describeClusters();
        regionalClusters.add(new RegionalCluster(response.clusters(), region));
      } catch (Exception e) {
        System.out.println(
            "Skipping Redshift discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return regionalClusters;
  }

  @Override
  public Map<String, String> getTagsForCluster(Cluster cluster) {
    return cluster.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
  }

  @Override
  public List<ResourceDetail> getAllRedshiftClustersWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();
    for (RegionalCluster regionalCluster : getAllRedshiftClusters(credentials)) {
      for (Cluster cluster : regionalCluster.clusters()) {
        Map<String, String> tags = getTagsForCluster(cluster);
        String name =
            ResourceDetail.resolveName(
                cluster.clusterIdentifier(), cluster.clusterIdentifier(), tags);
        resources.add(
            new ResourceDetail(
                cluster.clusterIdentifier(),
                name,
                "ClusterIdentifier",
                "AWS/Redshift",
                regionalCluster.region().id(),
                tags));
      }
    }
    return resources;
  }
}
