package com.cloudsherpa.ingestion.provider.aws.model;

import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;

public record RegionalDomain(List<DomainInfo> domains, Region region) {}
