package com.cloudsherpa.ingestion.unit.provider.aws.services.rds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDbInstance;
import com.cloudsherpa.ingestion.provider.aws.services.rds.AwsRdsService;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsClientBuilder;
import software.amazon.awssdk.services.rds.model.*;
import software.amazon.awssdk.services.rds.model.Tag;
import software.amazon.awssdk.services.rds.paginators.DescribeDBInstancesIterable;

class AwsRdsServiceTest {
  private DiscoveryExecutor discoveryExecutor;
  private AwsRdsService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsRdsService(discoveryExecutor);
  }

  private static <T> SdkIterable<T> sdkIterable(List<T> v) {
    return v::iterator;
  }

  @Test
  void getAllRdsInstances_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials c = mock(CloudCredentials.class);
    var expected =
        List.of(
            new com.cloudsherpa.ingestion.provider.aws.model.RegionalDbInstance(
                List.of(DBInstance.builder().dbInstanceIdentifier("db").build()),
                Region.US_EAST_1));
    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(i -> expected);
    assertSame(expected, service.getAllRdsInstances(c));
    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllRdsInstancesWithTags_shouldConvertInstances() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    DBInstance db =
        DBInstance.builder()
            .dbInstanceIdentifier("db-1")
            .dbInstanceArn("arn:aws:rds:us-east-1:123456789012:db:db-1")
            .build();

    RdsClientBuilder builder = mock(RdsClientBuilder.class);
    RdsClient rdsClient = mock(RdsClient.class);

    StaticCredentialsProvider credentialsProvider = mock(StaticCredentialsProvider.class);

    DescribeDBInstancesIterable paginator = mock(DescribeDBInstancesIterable.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);

    when(builder.credentialsProvider(credentialsProvider)).thenReturn(builder);

    when(builder.build()).thenReturn(rdsClient);

    when(rdsClient.describeDBInstancesPaginator()).thenReturn(paginator);

    when(paginator.dbInstances()).thenReturn(sdkIterable(List.of(db)));

    when(rdsClient.listTagsForResource(
            org.mockito.ArgumentMatchers
                .<java.util.function.Consumer<ListTagsForResourceRequest.Builder>>any()))
        .thenReturn(
            ListTagsForResourceResponse.builder()
                .tagList(Tag.builder().key("Name").value("database-prod").build())
                .build());

    when(discoveryExecutor.execute(eq(Region.regions()), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> worker = invocation.getArgument(1);

              return worker.apply(Region.US_EAST_1);
            });

    try (MockedStatic<RdsClient> rdsClientStatic = mockStatic(RdsClient.class);
        MockedStatic<AwsClientFactory> awsClientFactoryStatic =
            mockStatic(AwsClientFactory.class)) {

      rdsClientStatic.when(RdsClient::builder).thenReturn(builder);

      awsClientFactoryStatic
          .when(() -> AwsClientFactory.credentialsProvider(credentials))
          .thenReturn(credentialsProvider);

      List<ResourceDetail> result = service.getAllRdsInstancesWithTags(credentials);

      assertEquals(1, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("db-1", resource.getResourceId());
      assertEquals("db-1", resource.getName());
      assertEquals("DBInstanceIdentifier", resource.getResourceType());
      assertEquals("AWS/RDS", resource.getServiceCategory());
      assertEquals("us-east-1", resource.getRegion());

      assertEquals("database-prod", resource.getTags().get("Name"));

      verify(rdsClient).describeDBInstancesPaginator();

      verify(rdsClient)
          .listTagsForResource(
              org.mockito.ArgumentMatchers
                  .<java.util.function.Consumer<ListTagsForResourceRequest.Builder>>any());

      verify(rdsClient).close();
    }
  }

  @Test
  void getAllRdsInstancesWithTags_whenNoInstances_shouldReturnEmpty() {
    CloudCredentials c = mock(CloudCredentials.class);
    RdsClientBuilder b = mock(RdsClientBuilder.class);
    RdsClient client = mock(RdsClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    DescribeDBInstancesIterable paginator = mock(DescribeDBInstancesIterable.class);
    when(client.describeDBInstancesPaginator()).thenReturn(paginator);
    when(paginator.dbInstances()).thenReturn(sdkIterable(List.of()));

    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalDbInstance>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<RdsClient> m = mockStatic(RdsClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(RdsClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllRdsInstancesWithTags(c).isEmpty());
      verify(client).close();
    }
  }

  @Test
  void getAllRdsInstancesWithTags_whenTagLookupFails_shouldSkipInstance() {
    CloudCredentials c = mock(CloudCredentials.class);
    DBInstance db =
        DBInstance.builder().dbInstanceIdentifier("db-1").dbInstanceArn("arn:db").build();
    RdsClientBuilder b = mock(RdsClientBuilder.class);
    RdsClient client = mock(RdsClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    DescribeDBInstancesIterable paginator = mock(DescribeDBInstancesIterable.class);
    when(client.describeDBInstancesPaginator()).thenReturn(paginator);
    when(paginator.dbInstances()).thenReturn(sdkIterable(List.of(db)));
    when(client.listTagsForResource(any(ListTagsForResourceRequest.class)))
        .thenThrow(new RuntimeException("tag failure"));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalDbInstance>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<RdsClient> m = mockStatic(RdsClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(RdsClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllRdsInstancesWithTags(c).isEmpty());
      verify(client).close();
    }
  }
}
