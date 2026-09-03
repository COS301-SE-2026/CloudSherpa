package com.cloudsherpa.ingestion.unit.provider.aws.services.ecs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.aws.services.ecs.AwsEcsService;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.EcsClientBuilder;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.DescribeClustersRequest;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.Tag;
import software.amazon.awssdk.services.ecs.paginators.ListClustersIterable;

class AwsEcsServiceTest {
  private DiscoveryExecutor discoveryExecutor;
  private AwsEcsService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsEcsService(discoveryExecutor);
  }

  private static <T> SdkIterable<T> sdkIterable(List<T> values) {
    return values::iterator;
  }

  @Test
  void getAllEcsClusterArns_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials credentials = mock(CloudCredentials.class);
    List<RegionalArn> expected =
        List.of(new RegionalArn(List.of("arn:aws:ecs:us-east-1:123:cluster/a"), Region.US_EAST_1));
    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(inv -> expected);
    assertSame(expected, service.getAllEcsClusterArns(credentials));
    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllEcsClustersWithTags_shouldConvertClusters() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    Cluster cluster =
        Cluster.builder()
            .clusterName("cluster-1")
            .tags(Tag.builder().key("Name").value("production-cluster").build())
            .build();

    EcsClientBuilder builder = mock(EcsClientBuilder.class);
    EcsClient client = mock(EcsClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);
    ListClustersIterable clustersIterable = mock(ListClustersIterable.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);

    when(client.listClustersPaginator()).thenReturn(clustersIterable);
    when(clustersIterable.clusterArns())
        .thenReturn(sdkIterable(List.of("arn:aws:ecs:us-east-1:123:cluster/cluster-1")));

    when(client.describeClusters(ArgumentMatchers.<Consumer<DescribeClustersRequest.Builder>>any()))
        .thenReturn(DescribeClustersResponse.builder().clusters(cluster).build());

    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<EcsClient> mocked = mockStatic(EcsClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {

      mocked.when(EcsClient::builder).thenReturn(builder);

      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);

      List<ResourceDetail> result = service.getAllEcsClustersWithTags(credentials);

      assertEquals(1, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("cluster-1", resource.getResourceId());
      assertEquals("cluster-1", resource.getName());
      assertEquals("ClusterName", resource.getResourceType());
      assertEquals("AWS/ECS", resource.getServiceCategory());
      assertEquals("us-east-1", resource.getRegion());
      assertEquals("production-cluster", resource.getTags().get("Name"));

      verify(client).listClustersPaginator();

      verify(client)
          .describeClusters(ArgumentMatchers.<Consumer<DescribeClustersRequest.Builder>>any());

      verify(client).close();
    }
  }

  @Test
  void getAllEcsClustersWithTags_whenNoClusters_shouldReturnEmpty() {
    CloudCredentials credentials = mock(CloudCredentials.class);
    EcsClientBuilder builder = mock(EcsClientBuilder.class);
    EcsClient client = mock(EcsClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);
    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);
    ListClustersIterable clustersIterable = mock(ListClustersIterable.class);

    when(client.listClustersPaginator()).thenReturn(clustersIterable);
    when(clustersIterable.clusterArns()).thenReturn(sdkIterable(List.of()));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalArn>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<EcsClient> mocked = mockStatic(EcsClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {
      mocked.when(EcsClient::builder).thenReturn(builder);
      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);
      assertTrue(service.getAllEcsClustersWithTags(credentials).isEmpty());
      verify(client).close();
    }
  }

  @Test
  void getAllEcsClustersWithTags_whenClientFails_shouldReturnEmpty() {
    CloudCredentials credentials = mock(CloudCredentials.class);
    EcsClientBuilder builder = mock(EcsClientBuilder.class);
    EcsClient client = mock(EcsClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);
    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);
    when(client.listClustersPaginator()).thenThrow(new RuntimeException("AWS unavailable"));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalArn>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<EcsClient> mocked = mockStatic(EcsClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {
      mocked.when(EcsClient::builder).thenReturn(builder);
      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);
      assertTrue(service.getAllEcsClustersWithTags(credentials).isEmpty());
      verify(client).close();
    }
  }
}
