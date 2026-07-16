package com.cloudsherpa.ingestion.provider.aws.model;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;

public record RegionalInstance(Instance instance, Region region) {}
