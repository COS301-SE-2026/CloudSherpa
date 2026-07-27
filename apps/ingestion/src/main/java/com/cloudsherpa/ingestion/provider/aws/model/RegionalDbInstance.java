package com.cloudsherpa.ingestion.provider.aws.model;

import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.model.DBInstance;

public record RegionalDbInstance(List<DBInstance> domains, Region region) {}
