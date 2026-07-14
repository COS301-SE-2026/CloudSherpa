package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import java.util.List;
import software.amazon.awssdk.regions.Region;

public record RegionalArn(List<String> clusterArns, Region region) {}
