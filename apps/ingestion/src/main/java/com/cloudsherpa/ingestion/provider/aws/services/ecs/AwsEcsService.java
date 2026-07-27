package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ClusterField;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.Tag;

@Service
public class AwsEcsService implements EcsService {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final DiscoveryExecutor discoveryExecutor;

  public AwsEcsService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalArn> getAllEcsClusterArns(CloudCredentials credentials) {

    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClusterArns(region, credentials));
  }

  private List<RegionalArn> discoverClusterArns(Region region, CloudCredentials credentials) {

    List<RegionalArn> resources = new ArrayList<>();

    try (EcsClient ecs =
        EcsClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<String> clusterArns = ecs.listClustersPaginator().clusterArns().stream().toList();

      if (!clusterArns.isEmpty()) {
        resources.add(new RegionalArn(clusterArns, region));
      }

    } catch (Exception e) {
      logger.info("Skipping ECS discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }

  @Override
  public List<ResourceDetail> getAllEcsClustersWithTags(CloudCredentials credentials) {

    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClustersWithTags(region, credentials));
  }

  private List<ResourceDetail> discoverClustersWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (EcsClient ecs =
        EcsClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<String> clusterArns = ecs.listClustersPaginator().clusterArns().stream().toList();

      if (clusterArns.isEmpty()) {
        return resources;
      }

      DescribeClustersResponse response =
          ecs.describeClusters(request -> request.clusters(clusterArns).include(ClusterField.TAGS));

      resources.addAll(
          response.clusters().stream()
              .map(
                  cluster -> {
                    Map<String, String> tags =
                        cluster.tags().stream()
                            .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));

                    String name =
                        ResourceDetail.resolveName(
                            cluster.clusterName(), cluster.clusterName(), tags);

                    return new ResourceDetail(
                        name, name, "ClusterName", "AWS/ECS", region.id(), tags);
                  })
              .toList());

    } catch (Exception e) {
      logger.info("Skipping ECS discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }
}
