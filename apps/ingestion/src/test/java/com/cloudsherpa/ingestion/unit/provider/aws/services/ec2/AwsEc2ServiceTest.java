package com.cloudsherpa.ingestion.unit.provider.aws.services.ec2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalInstance;
import com.cloudsherpa.ingestion.provider.aws.services.ec2.AwsEc2Service;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;

class AwsEc2ServiceTest {

  private DiscoveryExecutor discoveryExecutor;
  private AwsEc2Service service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsEc2Service(discoveryExecutor);
  }

  @Test
  void getTagsForInstance_whenInstanceHasTags_shouldReturnTagMap() {
    Instance instance =
        Instance.builder()
            .instanceId("i-123456789")
            .tags(
                Tag.builder().key("Name").value("web-server").build(),
                Tag.builder().key("Environment").value("production").build())
            .build();

    Map<String, String> result = service.getTagsForInstance(instance);

    assertEquals(2, result.size());
    assertEquals("web-server", result.get("Name"));
    assertEquals("production", result.get("Environment"));
  }

  @Test
  void getTagsForInstance_whenInstanceHasNoTags_shouldReturnEmptyMap() {
    Instance instance = Instance.builder().instanceId("i-123456789").build();

    Map<String, String> result = service.getTagsForInstance(instance);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getTagsForInstance_whenDuplicateTagKeysExist_shouldUseLastValue() {
    Instance instance =
        Instance.builder()
            .instanceId("i-123456789")
            .tags(
                Tag.builder().key("Name").value("old-name").build(),
                Tag.builder().key("Name").value("new-name").build())
            .build();

    Map<String, String> result = service.getTagsForInstance(instance);

    assertEquals(1, result.size());
    assertEquals("new-name", result.get("Name"));
  }

  @Test
  void getAllEc2Instances_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    List<RegionalInstance> expected =
        List.of(
            new RegionalInstance(Instance.builder().instanceId("i-123").build(), Region.US_EAST_1));

    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(invocation -> expected);

    List<RegionalInstance> result = service.getAllEc2Instances(credentials);

    assertSame(expected, result);

    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllEc2InstancesWithTags_shouldConvertInstancesToResourceDetails() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    Instance instance =
        Instance.builder()
            .instanceId("i-123456789")
            .tags(
                Tag.builder().key("Name").value("my-server").build(),
                Tag.builder().key("Environment").value("production").build())
            .build();

    RegionalInstance regionalInstance =
        new RegionalInstance(instance, software.amazon.awssdk.regions.Region.US_EAST_1);

    // Stub the method through the discovery executor
    when(discoveryExecutor.execute(anyList(), any())).thenReturn(List.of(regionalInstance));

    List<ResourceDetail> result = service.getAllEc2InstancesWithTags(credentials);

    assertNotNull(result);
    assertEquals(1, result.size());

    ResourceDetail resource = result.get(0);

    assertEquals("i-123456789", resource.getResourceId());
    assertEquals("my-server", resource.getName());
    assertEquals("InstanceId", resource.getResourceType());
    assertEquals("AWS/EC2", resource.getServiceCategory());
    assertEquals("us-east-1", resource.getRegion());

    assertEquals("my-server", resource.getTags().get("Name"));
    assertEquals("production", resource.getTags().get("Environment"));
  }

  @Test
  void getAllEc2InstancesWithTags_whenNoInstances_shouldReturnEmptyList() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    when(discoveryExecutor.execute(anyList(), any())).thenReturn(List.of());

    List<ResourceDetail> result = service.getAllEc2InstancesWithTags(credentials);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllEc2InstancesWithTags_whenMultipleInstances_shouldConvertAllInstances() {
    CloudCredentials credentials = mock(CloudCredentials.class);

    Instance instance1 =
        Instance.builder()
            .instanceId("i-111")
            .tags(Tag.builder().key("Name").value("server-1").build())
            .build();

    Instance instance2 =
        Instance.builder()
            .instanceId("i-222")
            .tags(Tag.builder().key("Name").value("server-2").build())
            .build();

    when(discoveryExecutor.execute(anyList(), any()))
        .thenReturn(
            List.of(
                new RegionalInstance(instance1, software.amazon.awssdk.regions.Region.US_EAST_1),
                new RegionalInstance(instance2, software.amazon.awssdk.regions.Region.EU_WEST_1)));

    List<ResourceDetail> result = service.getAllEc2InstancesWithTags(credentials);

    assertEquals(2, result.size());

    assertEquals("i-111", result.get(0).getResourceId());
    assertEquals("server-1", result.get(0).getName());
    assertEquals("us-east-1", result.get(0).getRegion());

    assertEquals("i-222", result.get(1).getResourceId());
    assertEquals("server-2", result.get(1).getName());
    assertEquals("eu-west-1", result.get(1).getRegion());
  }

  @Test
  void getAllEc2Instances_shouldDiscoverInstancesFromEc2() {

    CloudCredentials credentials = mock(CloudCredentials.class);

    Instance instance1 = Instance.builder().instanceId("i-111").build();

    Instance instance2 = Instance.builder().instanceId("i-222").build();

    DescribeInstancesResponse response =
        DescribeInstancesResponse.builder()
            .reservations(Reservation.builder().instances(instance1, instance2).build())
            .build();

    Ec2ClientBuilder builder = mock(Ec2ClientBuilder.class);
    Ec2Client ec2Client = mock(Ec2Client.class);

    StaticCredentialsProvider credentialsProvider = mock(StaticCredentialsProvider.class);
    when(builder.region(Region.US_EAST_1)).thenReturn(builder);

    when(builder.credentialsProvider(credentialsProvider)).thenReturn(builder);

    when(builder.build()).thenReturn(ec2Client);

    when(ec2Client.describeInstances()).thenReturn(response);

    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              java.util.function.Function<Region, List<RegionalInstance>> function =
                  invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<Ec2Client> mockedEc2Client = mockStatic(Ec2Client.class);
        MockedStatic<AwsClientFactory> mockedAwsClientFactory =
            mockStatic(AwsClientFactory.class)) {

      mockedEc2Client.when(Ec2Client::builder).thenReturn(builder);

      mockedAwsClientFactory
          .when(() -> AwsClientFactory.credentialsProvider(credentials))
          .thenReturn(credentialsProvider);

      List<RegionalInstance> result = service.getAllEc2Instances(credentials);

      assertEquals(2, result.size());

      assertEquals("i-111", result.get(0).instance().instanceId());

      assertEquals("i-222", result.get(1).instance().instanceId());

      assertEquals(Region.US_EAST_1, result.get(0).region());

      assertEquals(Region.US_EAST_1, result.get(1).region());

      verify(ec2Client).describeInstances();
      verify(ec2Client).close();

      verify(builder).region(Region.US_EAST_1);
      verify(builder).credentialsProvider(credentialsProvider);
      verify(builder).build();
    }
  }

  @Test
  void getAllEc2Instances_whenDescribeInstancesThrowsException_shouldReturnEmptyList() {

    CloudCredentials credentials = mock(CloudCredentials.class);

    Ec2ClientBuilder builder = mock(Ec2ClientBuilder.class);
    Ec2Client ec2Client = mock(Ec2Client.class);

    StaticCredentialsProvider credentialsProvider = mock(StaticCredentialsProvider.class);

    when(builder.region(Region.US_EAST_1)).thenReturn(builder);

    when(builder.credentialsProvider(credentialsProvider)).thenReturn(builder);

    when(builder.build()).thenReturn(ec2Client);

    RuntimeException exception = new RuntimeException("AWS EC2 service unavailable");

    when(ec2Client.describeInstances()).thenThrow(exception);

    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalInstance>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<Ec2Client> mockedEc2Client = mockStatic(Ec2Client.class);
        MockedStatic<AwsClientFactory> mockedAwsClientFactory =
            mockStatic(AwsClientFactory.class)) {

      mockedEc2Client.when(Ec2Client::builder).thenReturn(builder);

      mockedAwsClientFactory
          .when(() -> AwsClientFactory.credentialsProvider(credentials))
          .thenReturn(credentialsProvider);

      List<RegionalInstance> result =
          assertDoesNotThrow(() -> service.getAllEc2Instances(credentials));

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(ec2Client).describeInstances();

      verify(ec2Client).close();

      verify(builder).region(Region.US_EAST_1);
      verify(builder).credentialsProvider(credentialsProvider);
      verify(builder).build();
    }
  }
}
