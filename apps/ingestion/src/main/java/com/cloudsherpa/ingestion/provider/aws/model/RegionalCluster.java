package com.cloudsherpa.ingestion.provider.aws.model;

import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.model.Cluster;

public record RegionalCluster(List<Cluster> clusters, Region region) {}
