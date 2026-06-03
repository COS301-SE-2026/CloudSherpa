package com.cloudsherpa.service.resourcediscovery.dto;

public record CloudCredentialsDto(
    String accessKey,
    String secretKey,
    String awsRegion,
    String tenantId,
    String clientId,
    String clientSecret) {}
