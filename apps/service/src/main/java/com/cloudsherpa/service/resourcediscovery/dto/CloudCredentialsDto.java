package com.cloudsherpa.service.resourcediscovery.dto;

public record CloudCredentialsDto(
    String accessKeyId,
    String secretAccessKey,
    String awsRegion,
    String tenantId,
    String clientId,
    String clientSecret) {}
