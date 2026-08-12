package com.cloudsherpa.service.persistconnection.provider.aws.dto;

public record BillingConfigDto(
    String bucketName, String bucketRegion, String exportPrefix, String exportName) {}
