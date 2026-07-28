package com.cloudsherpa.ingestion.provider.aws.services.elasticache;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.Tag;

@Service
public class AwsElastiCacheService implements ElastiCacheService {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final DiscoveryExecutor discoveryExecutor;

  public AwsElastiCacheService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalArn> getAllElastiCacheClusterArns(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClusterArns(region, credentials));
  }

  private List<RegionalArn> discoverClusterArns(Region region, CloudCredentials credentials) {
    List<RegionalArn> resources = new ArrayList<>();
    List<String> clusterArns = new ArrayList<>();

    try (ElastiCacheClient client =
        ElastiCacheClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      client.describeCacheClustersPaginator().cacheClusters().stream()
          .map(CacheCluster::arn)
          .filter(Objects::nonNull)
          .forEach(clusterArns::add);

      if (!clusterArns.isEmpty()) {
        resources.add(new RegionalArn(clusterArns, region));
      }

    } catch (Exception e) {
      logger.info(
          "Skipping ElastiCache discovery for region " + region.id() + ": " + e.getMessage());
    }
    return resources;
  }

  @Override
  public List<ResourceDetail> getAllElastiCacheClustersWithTags(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverClustersWithTags(region, credentials));
  }

  private void discoverClusterWithTags(
      ElastiCacheClient client,
      CacheCluster cluster,
      Region region,
      List<ResourceDetail> resources) {

    try {
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
              cluster.cacheClusterId(),
              name,
              "CacheClusterId",
              "AWS/ElastiCache",
              region.id(),
              tags));

    } catch (Exception e) {
      logger.info(
          "Skipping ElastiCache cluster "
              + cluster.cacheClusterId()
              + " in region "
              + region.id()
              + ": "
              + e.getMessage());
    }
  }

  private List<ResourceDetail> discoverClustersWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (ElastiCacheClient client =
        ElastiCacheClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      for (CacheCluster cluster : client.describeCacheClustersPaginator().cacheClusters()) {
        discoverClusterWithTags(client, cluster, region, resources);
      }

    } catch (Exception e) {
      logger.info(
          "Skipping ElastiCache discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }
}
