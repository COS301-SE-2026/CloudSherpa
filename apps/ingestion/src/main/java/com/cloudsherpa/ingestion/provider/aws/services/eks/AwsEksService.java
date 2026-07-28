package com.cloudsherpa.ingestion.provider.aws.services.eks;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;

@Service
public class AwsEksService implements EksService {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final DiscoveryExecutor discoveryExecutor;

  public AwsEksService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalArn> getAllEksClusterArns(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClusterArns(region, credentials));
  }

  private List<RegionalArn> discoverClusterArns(Region region, CloudCredentials credentials) {
    List<RegionalArn> resources = new ArrayList<>();

    try (EksClient eks =
        EksClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<String> clusterNames = eks.listClustersPaginator().clusters().stream().toList();

      if (!clusterNames.isEmpty()) {
        resources.add(new RegionalArn(clusterNames, region));
      }

    } catch (Exception e) {
      logger.info("Skipping EKS discovery for region " + region.id() + ": " + e.getMessage());
    }
    return resources;
  }

  @Override
  public List<ResourceDetail> getAllEksClustersWithTags(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClustersWithTags(region, credentials));
  }

  private void discoverClusterWithTags(
      EksClient eks, String clusterName, Region region, List<ResourceDetail> resources) {

    try {
      Cluster cluster = eks.describeCluster(r -> r.name(clusterName)).cluster();

      String name = ResourceDetail.resolveName(clusterName, cluster.name(), cluster.tags());

      resources.add(
          new ResourceDetail(
              clusterName, name, "ClusterName", "ContainerInsights", region.id(), cluster.tags()));

    } catch (Exception e) {
      logger.info(
          "Skipping EKS cluster "
              + clusterName
              + " in region "
              + region.id()
              + ": "
              + e.getMessage());
    }
  }

  private List<ResourceDetail> discoverClustersWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (EksClient eks =
        EksClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<String> clusterNames = eks.listClustersPaginator().clusters().stream().toList();

      for (String clusterName : clusterNames) {
        discoverClusterWithTags(eks, clusterName, region, resources);
      }

    } catch (Exception e) {
      logger.info("Skipping EKS discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }
}
