package com.cloudsherpa.ingestion.provider.aws.services.EcsService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.AwsClientFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ClusterField;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.Tag;

public class AwsEcsService implements EcsService {
  @Override
  public List<String> getAllEcsClusterArns(CloudCredentials credentials) {
    try (EcsClient ecs =
        EcsClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      return ecs.listClusters().clusterArns();
    }
  }

  @Override
  public List<ResourceDetail> getAllEcsClustersWithTags(CloudCredentials credentials) {
    try (EcsClient ecs =
        EcsClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<String> clusterArns = ecs.listClusters().clusterArns();

      DescribeClustersResponse response =
          ecs.describeClusters(r -> r.clusters(clusterArns).include(ClusterField.TAGS));

      return response.clusters().stream()
          .map(
              cluster -> {
                Map<String, String> tags =
                    cluster.tags().stream()
                        .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
                String name =
                    ResourceDetail.resolveName(cluster.clusterName(), cluster.clusterName(), tags);
                return new ResourceDetail(cluster.clusterArn(), name, "ClusterName", "ECS", tags);
              })
          .toList();
    }
  }
}
