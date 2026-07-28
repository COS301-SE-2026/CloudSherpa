package com.cloudsherpa.ingestion.provider.aws.services.opensearch;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDomain;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainResponse;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;
import software.amazon.awssdk.services.opensearch.model.ListDomainNamesRequest;
import software.amazon.awssdk.services.opensearch.model.ListTagsRequest;
import software.amazon.awssdk.services.opensearch.model.Tag;

@Service
public class AwsOpenSearchService implements OpenSearchService {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final DiscoveryExecutor discoveryExecutor;

  public AwsOpenSearchService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalDomain> getAllOpenSearchDomains(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverDomains(region, credentials));
  }

  private List<RegionalDomain> discoverDomains(Region region, CloudCredentials credentials) {
    List<RegionalDomain> resources = new ArrayList<>();

    try (OpenSearchClient client =
        OpenSearchClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<DomainInfo> domains =
          client.listDomainNames(ListDomainNamesRequest.builder().build()).domainNames();

      if (!domains.isEmpty()) {
        resources.add(new RegionalDomain(domains, region));
      }

    } catch (Exception e) {
      logger.info(
          "Skipping OpenSearch discovery for region " + region.id() + ": " + e.getMessage());
    }
    return resources;
  }

  @Override
  public List<ResourceDetail> getAllOpenSearchDomainsWithTags(CloudCredentials credentials) {

    return discoveryExecutor.execute(
        Region.regions(), region -> discoverDomainsWithTags(region, credentials));
  }

  private void discoverDomainWithTags(
      OpenSearchClient client,
      DomainInfo domainInfo,
      Region region,
      List<ResourceDetail> resources) {

    try {
      DescribeDomainResponse response =
          client.describeDomain(r -> r.domainName(domainInfo.domainName()));

      DomainStatus domain = response.domainStatus();

      Map<String, String> tags =
          client.listTags(ListTagsRequest.builder().arn(domain.arn()).build()).tagList().stream()
              .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));

      String name = ResourceDetail.resolveName(domain.domainName(), domain.domainName(), tags);

      resources.add(
          new ResourceDetail(domain.domainName(), name, "DomainName", "AWS/ES", region.id(), tags));

    } catch (Exception e) {
      logger.info(
          "Skipping OpenSearch domain "
              + domainInfo.domainName()
              + " in region "
              + region.id()
              + ": "
              + e.getMessage());
    }
  }

  private List<ResourceDetail> discoverDomainsWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (OpenSearchClient client =
        OpenSearchClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<DomainInfo> domains =
          client.listDomainNames(ListDomainNamesRequest.builder().build()).domainNames();

      for (DomainInfo domainInfo : domains) {
        discoverDomainWithTags(client, domainInfo, region, resources);
      }

    } catch (Exception e) {
      logger.info(
          "Skipping OpenSearch discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }
}
