package com.cloudsherpa.ingestion.unit.provider.aws.services.lambda;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.aws.services.lambda.AwsLambdaService;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.lambda.model.*;
import software.amazon.awssdk.services.lambda.paginators.ListFunctionsIterable;

class AwsLambdaServiceTest {
  private DiscoveryExecutor discoveryExecutor;
  private AwsLambdaService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsLambdaService(discoveryExecutor);
  }

  private static <T> SdkIterable<T> sdkIterable(List<T> v) {
    return v::iterator;
  }

  @Test
  void getAllLambdaFunctionArns_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials c = mock(CloudCredentials.class);
    var expected =
        List.of(
            new com.cloudsherpa.ingestion.provider.aws.model.RegionalArn(
                List.of("arn:lambda"), Region.US_EAST_1));
    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(i -> expected);
    assertSame(expected, service.getAllLambdaFunctionArns(c));
    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllLambdaFunctionsWithTags_shouldConvertFunctions() {
    CloudCredentials credentials = mock(CloudCredentials.class);
    FunctionConfiguration function =
        FunctionConfiguration.builder().functionName("fn-1").functionArn("arn:fn").build();
    LambdaClientBuilder builder = mock(LambdaClientBuilder.class);
    LambdaClient client = mock(LambdaClient.class);
    ListFunctionsIterable paginator = mock(ListFunctionsIterable.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);
    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);
    when(client.listFunctionsPaginator()).thenReturn(paginator);
    when(paginator.functions()).thenReturn(sdkIterable(List.of(function)));
    when(client.listTags(
            ArgumentMatchers.<java.util.function.Consumer<ListTagsRequest.Builder>>any()))
        .thenReturn(
            ListTagsResponse.builder().tags(java.util.Map.of("Name", "lambda-prod")).build());
    when(discoveryExecutor.execute(
            eq(Region.regions()), ArgumentMatchers.<Function<Region, List<ResourceDetail>>>any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> discovery = invocation.getArgument(1);
              return discovery.apply(Region.US_EAST_1);
            });
    try (MockedStatic<LambdaClient> mocked = mockStatic(LambdaClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {
      mocked.when(LambdaClient::builder).thenReturn(builder);
      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);
      List<ResourceDetail> result = service.getAllLambdaFunctionsWithTags(credentials);
      assertEquals(1, result.size());
      ResourceDetail resource = result.get(0);
      assertEquals("fn-1", resource.getResourceId());
      assertEquals("fn-1", resource.getName());
      assertEquals("FunctionName", resource.getResourceType());
      assertEquals("AWS/Lambda", resource.getServiceCategory());
      assertEquals("us-east-1", resource.getRegion());
      assertNotNull(resource.getTags());
      assertEquals(1, resource.getTags().size());
      assertEquals("lambda-prod", resource.getTags().get("Name"));
      verify(client).listFunctionsPaginator();
      verify(paginator).functions();
      verify(client)
          .listTags(ArgumentMatchers.<java.util.function.Consumer<ListTagsRequest.Builder>>any());
      verify(client).close();
    }
  }

  @Test
  void getAllLambdaFunctionsWithTags_whenNoFunctions_shouldReturnEmpty() {
    CloudCredentials c = mock(CloudCredentials.class);

    LambdaClientBuilder b = mock(LambdaClientBuilder.class);
    LambdaClient client = mock(LambdaClient.class);
    ListFunctionsIterable paginator = mock(ListFunctionsIterable.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);

    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);

    when(client.listFunctionsPaginator()).thenReturn(paginator);
    when(paginator.functions()).thenReturn(sdkIterable(List.of()));

    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> function = invocation.getArgument(1);
              return function.apply(Region.US_EAST_1);
            });

    try (MockedStatic<LambdaClient> m = mockStatic(LambdaClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {

      m.when(LambdaClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);

      assertTrue(service.getAllLambdaFunctionsWithTags(c).isEmpty());

      verify(client).close();
    }
  }

  @Test
  void getAllLambdaFunctionsWithTags_whenTagLookupFails_shouldSkipFunction() {
    CloudCredentials c = mock(CloudCredentials.class);
    FunctionConfiguration fn =
        FunctionConfiguration.builder().functionName("fn-1").functionArn("arn:fn").build();
    LambdaClientBuilder b = mock(LambdaClientBuilder.class);
    LambdaClient client = mock(LambdaClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    ListFunctionsIterable paginator = mock(ListFunctionsIterable.class);

    when(client.listFunctionsPaginator()).thenReturn(paginator);
    when(paginator.functions()).thenReturn(sdkIterable(List.of(fn)));
    when(client.listTags(any(ListTagsRequest.class)))
        .thenThrow(new RuntimeException("tag failure"));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalArn>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<LambdaClient> m = mockStatic(LambdaClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(LambdaClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllLambdaFunctionsWithTags(c).isEmpty());
      verify(client).close();
    }
  }
}
