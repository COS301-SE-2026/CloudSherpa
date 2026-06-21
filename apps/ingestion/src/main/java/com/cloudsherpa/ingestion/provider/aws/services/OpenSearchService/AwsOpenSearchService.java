package com.cloudsherpa.ingestion.provider.aws.services.OpenSearchService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.AwsClientFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainRequest;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainResponse;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;
import software.amazon.awssdk.services.opensearch.model.ListDomainNamesRequest;
import software.amazon.awssdk.services.opensearch.model.ListDomainNamesResponse;
import software.amazon.awssdk.services.opensearch.model.ListTagsRequest;
import software.amazon.awssdk.services.opensearch.model.Tag;

public class AwsOpenSearchService implements OpenSearchService {

  public List<DomainInfo> getAllOpenSearchDomains(CloudCredentials credentials) {
    List<DomainInfo> domains = new ArrayList<>();

    try (OpenSearchClient client =
        OpenSearchClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      ListDomainNamesResponse response =
          client.listDomainNames(ListDomainNamesRequest.builder().build());
      domains = response.domainNames();
    }
    return domains;
  }

  public List<ResourceDetail> getAllOpenSearchDomainsWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    try (OpenSearchClient client =
        OpenSearchClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      for (DomainInfo domainInfo : getAllOpenSearchDomains(credentials)) {

        DescribeDomainResponse domainResponse =
            client.describeDomain(
                DescribeDomainRequest.builder().domainName(domainInfo.domainName()).build());

        DomainStatus domain = domainResponse.domainStatus();

        Map<String, String> tags =
            client.listTags(ListTagsRequest.builder().arn(domain.arn()).build()).tagList().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
        String name = ResourceDetail.resolveName(domain.domainName(), domain.domainName(), tags);
        resources.add(
            new ResourceDetail(domain.domainName(), name, "DomainName", "OPENSEARCH", tags));
      }
    }
    return resources;
  }
}
