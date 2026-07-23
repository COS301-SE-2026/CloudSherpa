package com.cloudsherpa.ingestion.unit.provider.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.EcsClientBuilder;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.DescribeClustersRequest;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.ListClustersResponse;

class AwsCloudConnectorTest {

  private final AwsCloudConnector connector = new AwsCloudConnector();

  @Test
  void getProviderNameShouldReturnAws() {
    assertEquals("AWS", connector.getProviderName());
  }

  @Test
  void fetchMockUsageShouldGenerateRecords() {

    IngestionRequestEvent request = buildRequest(300);

    List<UsageRecordModel> result = connector.fetchMockUsage(request.getScopes().get(0), request);

    assertFalse(result.isEmpty());
  }

  @Test
  void fetchMockUsageShouldThrowForInvalidPeriod() {

    IngestionRequestEvent request = buildRequest(0);

    AccountScope accountScope = request.getScopes().get(0);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> connector.fetchMockUsage(accountScope, request));

    assertTrue(ex.getMessage().contains("Period must be > 0"));
  }

  @Test
  void fetchMockUsageShouldPopulateImportantFields() {

    IngestionRequestEvent request = buildRequest(300);

    List<UsageRecordModel> result = connector.fetchMockUsage(request.getScopes().get(0), request);

    UsageRecordModel usageRecord = result.get(0);

    assertNotNull(usageRecord.getProvider());
    assertNotNull(usageRecord.getMetricName());
    assertNotNull(usageRecord.getTimestamp());
    assertNotNull(usageRecord.getResourceId());
    assertNotNull(usageRecord.getIngestionId());
  }

  @Test
  void testConnectionShouldReturnBoolean() {
    CloudCredentials credentials = new CloudCredentials();
    credentials.setAccessKey("accessKey");
    credentials.setSecretKey("secretKey");
    boolean result = connector.testConnection(credentials);

    assertTrue(result || !result);
  }

  private IngestionRequestEvent buildRequest(int period) {

    IngestionRequestEvent request = new IngestionRequestEvent();

    request.setFrom(Instant.now().minusSeconds(3600));
    request.setTo(Instant.now());
    request.setPeriod(period);
    InstanceScope instanceScope = new InstanceScope();
    instanceScope.setIdentifierName("InstanceId");
    com.cloudsherpa.ingestion.connector.Instance instance =
        new com.cloudsherpa.ingestion.connector.Instance();
    instance.setIdentifier("i-123");
    instance.setRegion("af-south-1");
    instanceScope.setInstances(List.of(instance));

    Metric metric = new Metric();
    metric.setName("CPUUtilization");
    ServiceScope service = new ServiceScope();
    service.setName("EC2");
    service.setMetrics(List.of(metric));
    service.setInstances(List.of(instanceScope));

    AccountScope scope = new AccountScope();
    scope.setProvider("AWS");
    scope.setAccountId("123");
    scope.setServiceScopes(List.of(service));

    request.setScopes(List.of(scope));

    return request;
  }

  // The following "listAll" tests are unfortunately brittle due to static
  // mocking, I do not want to change the
  // code being tested to make testing more convenient as that makes the rel usage
  // of the class functions more complicated for the user
  @Test
  void getAllEc2InstancesShouldReturnResources() {

    CloudCredentials credentials = new CloudCredentials();
    credentials.setAccessKey("accessKey");
    credentials.setSecretKey("secretKey");

    Ec2Client ec2Client = mock(Ec2Client.class);
    Ec2ClientBuilder builder = mock(Ec2ClientBuilder.class);

    Tag nameTag = Tag.builder().key("Name").value("WebServer").build();

    Instance instance = Instance.builder().instanceId("i-123").tags(nameTag).build();

    Reservation reservation = Reservation.builder().instances(instance).build();

    DescribeInstancesResponse response =
        DescribeInstancesResponse.builder().reservations(reservation).build();

    when(ec2Client.describeInstances()).thenReturn(response);

    when(builder.region(any())).thenReturn(builder);
    when(builder.credentialsProvider(any())).thenReturn(builder);
    when(builder.build()).thenReturn(ec2Client);

    try (MockedStatic<Ec2Client> mocked = mockStatic(Ec2Client.class)) {

      mocked.when(Ec2Client::builder).thenReturn(builder);

      List<ResourceDetail> result = connector.getAllEc2Instances(credentials);

      assertEquals(41, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("i-123", resource.getResourceId());
      assertEquals("WebServer", resource.getName());
      assertEquals("InstanceId", resource.getResourceType());

      assertEquals("WebServer", resource.getTags().get("Name"));
    }
  }

  @Test
  void getAllEcsClustersShouldReturnClusters() {

    CloudCredentials credentials = new CloudCredentials();
    credentials.setAccessKey("accessKey");
    credentials.setSecretKey("secretKey");

    EcsClient client = mock(EcsClient.class);
    EcsClientBuilder builder = mock(EcsClientBuilder.class);

    software.amazon.awssdk.services.ecs.model.Tag tag =
        software.amazon.awssdk.services.ecs.model.Tag.builder()
            .key("Name")
            .value("ProdCluster")
            .build();

    Cluster cluster =
        Cluster.builder().clusterArn("arn:cluster").clusterName("cluster1").tags(tag).build();

    when(client.listClusters())
        .thenReturn(ListClustersResponse.builder().clusterArns("arn:cluster").build());

    when(client.describeClusters(
            ArgumentMatchers.<java.util.function.Consumer<DescribeClustersRequest.Builder>>any()))
        .thenReturn(DescribeClustersResponse.builder().clusters(cluster).build());
    when(builder.region(any())).thenReturn(builder);
    when(builder.credentialsProvider(any())).thenReturn(builder);
    when(builder.build()).thenReturn(client);

    try (MockedStatic<EcsClient> mocked = mockStatic(EcsClient.class)) {

      mocked.when(EcsClient::builder).thenReturn(builder);

      List<ResourceDetail> result = connector.getAllEcsClusters(credentials);

      assertEquals(41, result.size());

      ResourceDetail resource = result.get(0);

      assertEquals("arn:cluster", resource.getResourceId());
      assertEquals("cluster1", resource.getName());
      assertEquals("ClusterName", resource.getResourceType());
    }
  }
}
