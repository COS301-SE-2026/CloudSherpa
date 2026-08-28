package com.cloudsherpa.service.persistconnection.provider.azure.dto;

public record AzureCredentialsDto(
    String subscriptionId, String tenantId, String clientId, String clientSecret) {}
