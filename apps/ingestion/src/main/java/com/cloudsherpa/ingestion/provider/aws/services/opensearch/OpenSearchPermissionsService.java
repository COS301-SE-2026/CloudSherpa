package com.cloudsherpa.ingestion.provider.aws.services.opensearch;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchPermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of(
        "opensearch:ListDomainNames",
        "opensearch:DescribeDomain",
        "opensearch:DescribeDomains",
        "opensearch:ListTags");
  }
}
