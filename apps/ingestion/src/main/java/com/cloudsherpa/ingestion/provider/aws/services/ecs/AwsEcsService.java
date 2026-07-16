package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ClusterField;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.Tag;

public class AwsEcsService implements EcsService {
  @Override
  public List<RegionalArn> getAllEcsClusterArns(CloudCredentials credentials) {
    List<RegionalArn> regionalArns = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (EcsClient ecs =
          EcsClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        regionalArns.add(new RegionalArn(ecs.listClusters().clusterArns(), region));
      } catch (Exception e) {
        System.out.println(
            "Skipping ECS discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return regionalArns;
  }

  @Override
  public List<ResourceDetail> getAllEcsClustersWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (EcsClient ecs =
          EcsClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        List<String> clusterArns = ecs.listClusters().clusterArns();

        DescribeClustersResponse response =
            ecs.describeClusters(r -> r.clusters(clusterArns).include(ClusterField.TAGS));

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
                          cluster.clusterArn(), name, "ClusterName", "ECS", region.id(), tags);
                    })
                .toList());
      }
    }
    return resources;
  }
}
