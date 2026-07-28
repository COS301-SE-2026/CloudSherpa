package com.cloudsherpa.service.persistconnection.aws.dto;

public record BillingConfigDto(
    String bucketName, String bucketRegion, String exportPrefix, String exportName) {}
