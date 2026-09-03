package com.cloudsherpa.ingestion.unit.provider.aws.services.opensearch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDomain;
import com.cloudsherpa.ingestion.provider.aws.services.opensearch.AwsOpenSearchService;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.OpenSearchClientBuilder;
import software.amazon.awssdk.services.opensearch.model.*;
import software.amazon.awssdk.services.opensearch.model.Tag;

class AwsOpenSearchServiceTest {
  private DiscoveryExecutor discoveryExecutor;
  private AwsOpenSearchService service;

  @BeforeEach
  void setUp() {
    discoveryExecutor = mock(DiscoveryExecutor.class);
    service = new AwsOpenSearchService(discoveryExecutor);
  }

  @Test
  void getAllOpenSearchDomains_shouldDelegateToDiscoveryExecutor() {
    CloudCredentials c = mock(CloudCredentials.class);
    var expected =
        List.of(
            new com.cloudsherpa.ingestion.provider.aws.model.RegionalDomain(
                List.of(DomainInfo.builder().domainName("domain").build()), Region.US_EAST_1));
    when(discoveryExecutor.execute(anyList(), any())).thenAnswer(i -> expected);
    assertSame(expected, service.getAllOpenSearchDomains(c));
    verify(discoveryExecutor).execute(eq(Region.regions()), any());
  }

  @Test
  void getAllOpenSearchDomainsWithTags_shouldConvertDomains() {
    CloudCredentials credentials = mock(CloudCredentials.class);
    DomainInfo domainInfo = DomainInfo.builder().domainName("domain-1").build();
    DomainStatus domainStatus =
        DomainStatus.builder()
            .domainName("domain-1")
            .arn("arn:aws:es:us-east-1:123456789012:domain/domain-1")
            .build();
    OpenSearchClientBuilder builder = mock(OpenSearchClientBuilder.class);
    OpenSearchClient client = mock(OpenSearchClient.class);
    StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);
    when(builder.region(Region.US_EAST_1)).thenReturn(builder);
    when(builder.credentialsProvider(provider)).thenReturn(builder);
    when(builder.build()).thenReturn(client);
    when(client.listDomainNames(any(ListDomainNamesRequest.class)))
        .thenReturn(ListDomainNamesResponse.builder().domainNames(domainInfo).build());
    when(client.describeDomain(ArgumentMatchers.<Consumer<DescribeDomainRequest.Builder>>any()))
        .thenReturn(DescribeDomainResponse.builder().domainStatus(domainStatus).build());
    when(client.listTags(any(ListTagsRequest.class)))
        .thenReturn(
            ListTagsResponse.builder()
                .tagList(Tag.builder().key("Name").value("search-prod").build())
                .build());
    when(discoveryExecutor.execute(
            eq(Region.regions()), ArgumentMatchers.<Function<Region, List<ResourceDetail>>>any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<ResourceDetail>> discovery = invocation.getArgument(1);
              return discovery.apply(Region.US_EAST_1);
            });
    try (MockedStatic<OpenSearchClient> mocked = mockStatic(OpenSearchClient.class);
        MockedStatic<AwsClientFactory> factory = mockStatic(AwsClientFactory.class)) {
      mocked.when(OpenSearchClient::builder).thenReturn(builder);
      factory.when(() -> AwsClientFactory.credentialsProvider(credentials)).thenReturn(provider);
      List<ResourceDetail> result = service.getAllOpenSearchDomainsWithTags(credentials);
      assertEquals(1, result.size());
      ResourceDetail resource = result.get(0);
      assertEquals("domain-1", resource.getResourceId());
      assertEquals("domain-1", resource.getName());
      assertEquals("DomainName", resource.getResourceType());
      assertEquals("AWS/ES", resource.getServiceCategory());
      assertEquals("us-east-1", resource.getRegion());
      assertNotNull(resource.getTags());
      assertEquals(1, resource.getTags().size());
      assertEquals("search-prod", resource.getTags().get("Name"));
      verify(client).listDomainNames(any(ListDomainNamesRequest.class));
      verify(client)
          .describeDomain(ArgumentMatchers.<Consumer<DescribeDomainRequest.Builder>>any());
      verify(client).listTags(any(ListTagsRequest.class));
      verify(client).close();
    }
  }

  @Test
  void getAllOpenSearchDomainsWithTags_whenNoDomains_shouldReturnEmpty() {
    CloudCredentials c = mock(CloudCredentials.class);
    OpenSearchClientBuilder b = mock(OpenSearchClientBuilder.class);
    OpenSearchClient client = mock(OpenSearchClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    when(client.listDomainNames(any(ListDomainNamesRequest.class)))
        .thenReturn(ListDomainNamesResponse.builder().build());
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalDomain>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<OpenSearchClient> m = mockStatic(OpenSearchClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(OpenSearchClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllOpenSearchDomainsWithTags(c).isEmpty());
      verify(client).close();
    }
  }

  @Test
  void getAllOpenSearchDomainsWithTags_whenDomainTagLookupFails_shouldSkipDomain() {
    CloudCredentials c = mock(CloudCredentials.class);
    DomainInfo info = DomainInfo.builder().domainName("domain-1").build();
    DomainStatus status = DomainStatus.builder().domainName("domain-1").arn("arn:domain").build();
    OpenSearchClientBuilder b = mock(OpenSearchClientBuilder.class);
    OpenSearchClient client = mock(OpenSearchClient.class);
    StaticCredentialsProvider p = mock(StaticCredentialsProvider.class);
    when(b.region(Region.US_EAST_1)).thenReturn(b);
    when(b.credentialsProvider(p)).thenReturn(b);
    when(b.build()).thenReturn(client);
    when(client.listDomainNames(any(ListDomainNamesRequest.class)))
        .thenReturn(ListDomainNamesResponse.builder().domainNames(info).build());
    when(client.describeDomain(any(DescribeDomainRequest.class)))
        .thenReturn(DescribeDomainResponse.builder().domainStatus(status).build());
    when(client.listTags(any(ListTagsRequest.class)))
        .thenThrow(new RuntimeException("tag failure"));
    when(discoveryExecutor.execute(anyList(), any()))
        .thenAnswer(
            invocation -> {
              Function<Region, List<RegionalDomain>> function = invocation.getArgument(1);

              return function.apply(Region.US_EAST_1);
            });
    try (MockedStatic<OpenSearchClient> m = mockStatic(OpenSearchClient.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(OpenSearchClient::builder).thenReturn(b);
      f.when(() -> AwsClientFactory.credentialsProvider(c)).thenReturn(p);
      assertTrue(service.getAllOpenSearchDomainsWithTags(c).isEmpty());
      verify(client).close();
    }
  }
}
