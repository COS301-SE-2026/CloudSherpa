package com.cloudsherpa.ingestion.provider.aws.services.redshift;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalCluster;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.Tag;

@Service
public class AwsRedshiftService implements RedshiftService {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final DiscoveryExecutor discoveryExecutor;

  public AwsRedshiftService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalCluster> getAllRedshiftClusters(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClusters(region, credentials));
  }

  private List<RegionalCluster> discoverClusters(Region region, CloudCredentials credentials) {
    List<RegionalCluster> resources = new ArrayList<>();

    try (RedshiftClient client =
        RedshiftClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<Cluster> clusters = client.describeClustersPaginator().clusters().stream().toList();

      if (!clusters.isEmpty()) {
        resources.add(new RegionalCluster(clusters, region));
      }
    } catch (Exception e) {

      logger.info("Skipping Redshift discovery for region " + region.id() + ": " + e.getMessage());
    }
    return resources;
  }

  @Override
  public Map<String, String> getTagsForCluster(Cluster cluster) {
    return cluster.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
  }

  @Override
  public List<ResourceDetail> getAllRedshiftClustersWithTags(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClustersWithTags(region, credentials));
  }

  private void discoverClusterWithTags(
      Cluster cluster, Region region, List<ResourceDetail> resources) {

    try {
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
              region.id(),
              tags));

    } catch (Exception e) {
      logger.info(
          "Skipping Redshift cluster "
              + cluster.clusterIdentifier()
              + " in region "
              + region.id()
              + ": "
              + e.getMessage());
    }
  }

  private List<ResourceDetail> discoverClustersWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (RedshiftClient client =
        RedshiftClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      for (Cluster cluster : client.describeClustersPaginator().clusters()) {
        discoverClusterWithTags(cluster, region, resources);
      }

    } catch (Exception e) {
      logger.info("Skipping Redshift discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }
}
