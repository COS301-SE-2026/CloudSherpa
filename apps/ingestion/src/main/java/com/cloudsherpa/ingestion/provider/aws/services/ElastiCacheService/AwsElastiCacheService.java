package com.cloudsherpa.ingestion.provider.aws.services.ElastiCacheService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersResponse;
import software.amazon.awssdk.services.elasticache.model.Tag;

public class AwsElastiCacheService implements ElastiCacheService {
  @Override
  public List<String> getAllElastiCacheClusterArns(CloudCredentials credentials) {
    List<String> clusterArns = new ArrayList<>();
    try (ElastiCacheClient client =
        ElastiCacheClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {
      DescribeCacheClustersResponse response = client.describeCacheClusters();

      for (CacheCluster cluster : response.cacheClusters()) {
        if (cluster.arn() != null) {
          clusterArns.add(cluster.arn());
        }
      }
    }

    return clusterArns;
  }

  @Override
  public List<ResourceDetail> getAllElastiCacheClustersWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    try (ElastiCacheClient client =
        ElastiCacheClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      DescribeCacheClustersResponse response = client.describeCacheClusters();

      for (CacheCluster cluster : response.cacheClusters()) {

        Map<String, String> tags = Collections.emptyMap();

        if (cluster.arn() != null) {
          tags =
              client.listTagsForResource(r -> r.resourceName(cluster.arn())).tagList().stream()
                  .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
        }
        String name =
            ResourceDetail.resolveName(cluster.cacheClusterId(), cluster.cacheClusterId(), tags);
        resources.add(
            new ResourceDetail(
                cluster.cacheClusterId(), name, "CacheClusterId", "ELASTICACHE", tags));
      }
    }

    return resources;
  }
}
