package com.cloudsherpa.ingestion.unit.provider.aws.services.eks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.aws.services.eks.AwsEksService;
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
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.EksClientBuilder;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;
import software.amazon.awssdk.services.eks.model.DescribeClusterResponse;
import software.amazon.awssdk.services.eks.paginators.ListClustersIterable;

class AwsEksServiceTest {

  private DiscoveryExecutor discoveryExecutor;
  private AwsEksService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsEksService(discoveryExecutor);
  }

  private static <T> SdkIterable<T> sdkIterable(List<T> values) {
    return values::iterator;
  }

  @Test
  void getAllEksClusterArns_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    List<RegionalArn> expected = List.of(new RegionalArn(List.of("cluster-1"), Region.US_EAST_1));

    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(invocation -> expected);

    assertSame(expected, service.getAllEksClusterArns(credentials));

    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllEksClustersWithTags_shouldConvertClusters() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    Cluster cluster =
        Cluster.builder()
            .name("cluster-1")
            .tags(java.util.Map.of("Name", "eks-production"))
            .build();

    EksClientBuilder builder = mock(EksClientBuilder.class);
    EksClient client = mock(EksClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);
    ListClustersIterable paginator = mock(ListClustersIterable.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);

    when(builder.credentialsProvider(provider)).thenReturn(builder);

    when(builder.build()).thenReturn(client);

    when(client.listClustersPaginator()).thenReturn(paginator);

    when(paginator.clusters()).thenReturn(() -> List.of("cluster-1").iterator());

    when(client.describeCluster(ArgumentMatchers.<Consumer<DescribeClusterRequest.Builder>>any()))
        .thenReturn(DescribeClusterResponse.builder().cluster(cluster).build());
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<EksClient> mocked = mockStatic(EksClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {

      mocked.when(EksClient::builder).thenReturn(builder);

      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);

      List<ResourceDetail> result = service.getAllEksClustersWithTags(credentials);

      assertEquals(1, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("cluster-1", resource.getResourceId());
      assertEquals("cluster-1", resource.getName());
      assertEquals("ClusterName", resource.getResourceType());
      assertEquals("ContainerInsights", resource.getServiceCategory());
      assertEquals("us-east-1", resource.getRegion());

      verify(client).listClustersPaginator();
      verify(paginator).clusters();

      verify(client)
          .describeCluster(ArgumentMatchers.<Consumer<DescribeClusterRequest.Builder>>any());

      verify(client).close();
    }
  }

  @Test
  void getAllEksClustersWithTags_whenNoClusters_shouldReturnEmpty() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    EksClientBuilder builder = mock(EksClientBuilder.class);
    EksClient client = mock(EksClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);

    ListClustersIterable paginator = mock(ListClustersIterable.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);

    when(builder.credentialsProvider(provider)).thenReturn(builder);

    when(builder.build()).thenReturn(client);

    when(client.listClustersPaginator()).thenReturn(paginator);

    when(paginator.clusters()).thenReturn(sdkIterable(List.of()));

    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalArn>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<EksClient> mocked = mockStatic(EksClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {

      mocked.when(EksClient::builder).thenReturn(builder);

      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);

      assertTrue(service.getAllEksClustersWithTags(credentials).isEmpty());

      verify(client).close();
    }
  }

  @Test
  void getAllEksClustersWithTags_whenClientFails_shouldReturnEmpty() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    EksClientBuilder builder = mock(EksClientBuilder.class);
    EksClient client = mock(EksClient.class);
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

    try (MockedStatic<EksClient> mocked = mockStatic(EksClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {

      mocked.when(EksClient::builder).thenReturn(builder);

      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);

      assertTrue(service.getAllEksClustersWithTags(credentials).isEmpty());

      verify(client).close();
    }
  }
}
