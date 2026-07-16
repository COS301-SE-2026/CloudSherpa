package com.cloudsherpa.ingestion.provider.aws.services.opensearch;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.regions.Region;
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
  @Override
  public List<RegionalDomain> getAllOpenSearchDomains(CloudCredentials credentials) {
    List<DomainInfo> domains = new ArrayList<>();
    List<RegionalDomain> regionalDomains = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (OpenSearchClient client =
          OpenSearchClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        ListDomainNamesResponse response =
            client.listDomainNames(ListDomainNamesRequest.builder().build());
        domains = response.domainNames();
        regionalDomains.add(new RegionalDomain(domains, region));
      } catch (Exception e) {
        System.out.println(
            "Skipping OpenSearch discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return regionalDomains;
  }

  @Override
  public List<ResourceDetail> getAllOpenSearchDomainsWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    for (RegionalDomain regionalDomainInfo : getAllOpenSearchDomains(credentials)) {
      try (OpenSearchClient client =
          OpenSearchClient.builder()
              .region(regionalDomainInfo.region())
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {
        for (DomainInfo domainInfo : regionalDomainInfo.domains()) {
          DescribeDomainResponse domainResponse =
              client.describeDomain(
                  DescribeDomainRequest.builder().domainName(domainInfo.domainName()).build());

          DomainStatus domain = domainResponse.domainStatus();

          Map<String, String> tags =
              client
                  .listTags(ListTagsRequest.builder().arn(domain.arn()).build())
                  .tagList()
                  .stream()
                  .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
          String name = ResourceDetail.resolveName(domain.domainName(), domain.domainName(), tags);
          resources.add(
              new ResourceDetail(
                  domain.domainName(),
                  name,
                  "DomainName",
                  "OPENSEARCH",
                  regionalDomainInfo.region().id(),
                  tags));
        }
      } catch (Exception e) {

      }
    }
    return resources;
  }
}
