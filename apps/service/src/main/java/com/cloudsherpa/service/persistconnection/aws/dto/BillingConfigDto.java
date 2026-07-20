package com.cloudsherpa.service.persistconnection.aws.dto;

public record BillingConfigDto(String bucketName, String exportPrefix, String exportName) {}
