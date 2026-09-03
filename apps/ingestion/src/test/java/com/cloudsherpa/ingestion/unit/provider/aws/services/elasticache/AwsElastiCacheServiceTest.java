package com.cloudsherpa.ingestion.unit.provider.aws.services.elasticache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.aws.services.elasticache.AwsElastiCacheService;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.ElastiCacheClientBuilder;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.elasticache.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.elasticache.model.Tag;
import software.amazon.awssdk.services.elasticache.paginators.DescribeCacheClustersIterable;

class AwsElastiCacheServiceTest {

  private DiscoveryExecutor discoveryExecutor;
  private AwsElastiCacheService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsElastiCacheService(discoveryExecutor);
  }

  private static <T> SdkIterable<T> sdkIterable(List<T> values) {
    return values::iterator;
  }

  @Test
  void getAllElastiCacheClusterArns_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    List<RegionalArn> expected =
        List.of(
            new RegionalArn(
                List.of("arn:aws:elasticache:us-east-1:123456789012:cluster:cache-1"),
                Region.US_EAST_1));

    when(discoveryExecutor.execute(
            eq(Region.regions()), ArgumentMatchers.<Function<Region, List<RegionalArn>>>any()))
        .thenReturn(expected);

    List<RegionalArn> result = service.getAllElastiCacheClusterArns(credentials);

    assertSame(expected, result);

    verify(discoveryExecutor)
        .execute(eq(Region.regions()), ArgumentMatchers.<Function<Region, List<RegionalArn>>>any());
  }

  @Test
  void getAllElastiCacheClustersWithTags_shouldConvertClusters() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    CacheCluster cluster =
        CacheCluster.builder()
            .cacheClusterId("cache-1")
            .arn("arn:aws:elasticache:us-east-1:123456789012:cluster:cache-1")
            .build();

    ElastiCacheClientBuilder builder = mock(ElastiCacheClientBuilder.class);
    ElastiCacheClient client = mock(ElastiCacheClient.class);
    DescribeCacheClustersIterable paginator = mock(DescribeCacheClustersIterable.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);

    when(client.describeCacheClustersPaginator()).thenReturn(paginator);
    when(paginator.cacheClusters()).thenReturn(sdkIterable(List.of(cluster)));

    ListTagsForResourceResponse tagsResponse =
        ListTagsForResourceResponse.builder()
            .tagList(Tag.builder().key("Name").value("redis-prod").build())
            .build();

    when(client.listTagsForResource(
            ArgumentMatchers
                .<java.util.function.Consumer<ListTagsForResourceRequest.Builder>>any()))
        .thenReturn(tagsResponse);

    when(discoveryExecutor.execute(
            eq(Region.regions()), ArgumentMatchers.<Function<Region, List<ResourceDetail>>>any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> worker = invocation.getArgument(1);

              return worker.apply(Region.US_EAST_1);
            });

    try (MockedStatic<ElastiCacheClient> elasticacheMock = mockStatic(ElastiCacheClient.class);
        MockedStatic<AwsClientFactory> factoryMock = mockStatic(AwsClientFactory.class)) {

      elasticacheMock.when(ElastiCacheClient::builder).thenReturn(builder);

      factoryMock
          .when(() -> AwsClientFactory.credentialsProvider(credentials))
          .thenReturn(provider);

      List<ResourceDetail> result = service.getAllElastiCacheClustersWithTags(credentials);

      assertEquals(1, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("cache-1", resource.getResourceId());
      assertEquals("cache-1", resource.getName());
      assertEquals("CacheClusterId", resource.getResourceType());
      assertEquals("AWS/ElastiCache", resource.getServiceCategory());
      assertEquals("us-east-1", resource.getRegion());

      assertEquals(1, resource.getTags().size());
      assertEquals("redis-prod", resource.getTags().get("Name"));

      verify(client).describeCacheClustersPaginator();
      verify(paginator).cacheClusters();

      verify(client)
          .listTagsForResource(
              ArgumentMatchers
                  .<java.util.function.Consumer<ListTagsForResourceRequest.Builder>>any());

      verify(client).close();
    }
  }

  @Test
  void getAllElastiCacheClustersWithTags_whenClusterHasNoArn_shouldUseEmptyTags() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    CacheCluster cluster = CacheCluster.builder().cacheClusterId("cache-1").build();

    ElastiCacheClientBuilder builder = mock(ElastiCacheClientBuilder.class);
    ElastiCacheClient client = mock(ElastiCacheClient.class);
    DescribeCacheClustersIterable paginator = mock(DescribeCacheClustersIterable.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);

    when(client.describeCacheClustersPaginator()).thenReturn(paginator);

    when(paginator.cacheClusters()).thenReturn(sdkIterable(List.of(cluster)));

    when(discoveryExecutor.execute(
            eq(Region.regions()), ArgumentMatchers.<Function<Region, List<ResourceDetail>>>any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<ElastiCacheClient> elasticacheMock = mockStatic(ElastiCacheClient.class);
        MockedStatic<AwsClientFactory> factoryMock = mockStatic(AwsClientFactory.class)) {

      elasticacheMock.when(ElastiCacheClient::builder).thenReturn(builder);

      factoryMock
          .when(() -> AwsClientFactory.credentialsProvider(credentials))
          .thenReturn(provider);

      List<ResourceDetail> result = service.getAllElastiCacheClustersWithTags(credentials);

      assertEquals(1, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("cache-1", resource.getResourceId());
      assertEquals("cache-1", resource.getName());
      assertTrue(resource.getTags().isEmpty());

      verify(client).describeCacheClustersPaginator();
      verify(paginator).cacheClusters();

      verify(client, never()).listTagsForResource(any(ListTagsForResourceRequest.class));

      verify(client).close();
    }
  }

  @Test
  void getAllElastiCacheClustersWithTags_whenClientFails_shouldReturnEmpty() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    ElastiCacheClientBuilder builder = mock(ElastiCacheClientBuilder.class);
    ElastiCacheClient client = mock(ElastiCacheClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);

    when(client.describeCacheClustersPaginator())
        .thenThrow(new RuntimeException("AWS unavailable"));

    when(discoveryExecutor.execute(
            eq(Region.regions()), ArgumentMatchers.<Function<Region, List<ResourceDetail>>>any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<ElastiCacheClient> elasticacheMock = mockStatic(ElastiCacheClient.class);
        MockedStatic<AwsClientFactory> factoryMock = mockStatic(AwsClientFactory.class)) {

      elasticacheMock.when(ElastiCacheClient::builder).thenReturn(builder);

      factoryMock
          .when(() -> AwsClientFactory.credentialsProvider(credentials))
          .thenReturn(provider);

      List<ResourceDetail> result = service.getAllElastiCacheClustersWithTags(credentials);

      assertNotNull(result);
      assertTrue(result.isEmpty());

      verify(client).describeCacheClustersPaginator();
      verify(client).close();
    }
  }
}
