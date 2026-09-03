package com.cloudsherpa.ingestion.unit.provider.aws.services.redshift;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalCluster;
import com.cloudsherpa.ingestion.provider.aws.services.redshift.AwsRedshiftService;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.RedshiftClientBuilder;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.paginators.DescribeClustersIterable;

class AwsRedshiftServiceTest {
  private DiscoveryExecutor discoveryExecutor;
  private AwsRedshiftService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsRedshiftService(discoveryExecutor);
  }

  private static <T> SdkIterable<T> sdkIterable(List<T> v) {
    return v::iterator;
  }

  @Test
  void getTagsForCluster_whenTagsExist_shouldReturnTagMap() {
    Cluster c =
        Cluster.builder()
            .tags(
                Tag.builder().key("Name").value("prod").build(),
                Tag.builder().key("Env").value("production").build())
            .build();
    Map<String, String> r = service.getTagsForCluster(c);
    assertEquals(2, r.size());
    assertEquals("prod", r.get("Name"));
    assertEquals("production", r.get("Env"));
  }

  @Test
  void getTagsForCluster_whenNoTags_shouldReturnEmptyMap() {
    assertTrue(service.getTagsForCluster(Cluster.builder().build()).isEmpty());
  }

  @Test
  void getTagsForCluster_whenDuplicateKeys_shouldUseLastValue() {
    Cluster c =
        Cluster.builder()
            .tags(
                Tag.builder().key("Name").value("old").build(),
                Tag.builder().key("Name").value("new").build())
            .build();
    assertEquals("new", service.getTagsForCluster(c).get("Name"));
  }

  @Test
  void getAllRedshiftClusters_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials c = mock(CloudCredentials.class);
    var expected =
        List.of(
            new com.cloudsherpa.ingestion.provider.aws.model.RegionalCluster(
                List.of(Cluster.builder().clusterIdentifier("c").build()), Region.US_EAST_1));
    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(i -> expected);
    assertSame(expected, service.getAllRedshiftClusters(c));
    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllRedshiftClustersWithTags_shouldConvertClusters() {
    CloudCredentials c = mock(CloudCredentials.class);
    Cluster x =
        Cluster.builder()
            .clusterIdentifier("cluster-1")
            .tags(Tag.builder().key("Name").value("redshift-prod").build())
            .build();
    RedshiftClientBuilder b = mock(RedshiftClientBuilder.class);
    RedshiftClient client = mock(RedshiftClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    DescribeClustersIterable paginator = mock(DescribeClustersIterable.class);
    when(client.describeClustersPaginator()).thenReturn(paginator);
    when(paginator.clusters()).thenReturn(sdkIterable(List.of(x)));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalCluster>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<RedshiftClient> m = mockStatic(RedshiftClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(RedshiftClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      var r = service.getAllRedshiftClustersWithTags(c);
      assertEquals(1, r.size());
      assertEquals("cluster-1", r.get(0).getResourceId());
      assertEquals("cluster-1", r.get(0).getName());
      assertEquals("ClusterIdentifier", r.get(0).getResourceType());
      assertEquals("AWS/Redshift", r.get(0).getServiceCategory());
      assertEquals("us-east-1", r.get(0).getRegion());
      verify(client).close();
    }
  }

  @Test
  void getAllRedshiftClustersWithTags_whenNoClusters_shouldReturnEmpty() {
    CloudCredentials c = mock(CloudCredentials.class);
    RedshiftClientBuilder b = mock(RedshiftClientBuilder.class);
    RedshiftClient client = mock(RedshiftClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    DescribeClustersIterable paginator = mock(DescribeClustersIterable.class);
    when(client.describeClustersPaginator()).thenReturn(paginator);
    when(paginator.clusters()).thenReturn(sdkIterable(List.of()));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalCluster>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<RedshiftClient> m = mockStatic(RedshiftClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(RedshiftClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllRedshiftClustersWithTags(c).isEmpty());
      verify(client).close();
    }
  }

  @Test
  void getAllRedshiftClustersWithTags_whenClientFails_shouldReturnEmpty() {
    CloudCredentials c = mock(CloudCredentials.class);
    RedshiftClientBuilder b = mock(RedshiftClientBuilder.class);
    RedshiftClient client = mock(RedshiftClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    when(client.describeClustersPaginator()).thenThrow(new RuntimeException("AWS unavailable"));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalCluster>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<RedshiftClient> m = mockStatic(RedshiftClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(RedshiftClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllRedshiftClustersWithTags(c).isEmpty());
      verify(client).close();
    }
  }
}
