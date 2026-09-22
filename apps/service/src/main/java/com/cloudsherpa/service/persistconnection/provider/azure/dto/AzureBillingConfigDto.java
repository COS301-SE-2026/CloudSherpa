package com.cloudsherpa.service.persistconnection.provider.azure.dto;

public record AzureBillingConfigDto(
    String storageAccountName,
    String blobContainerName,
    String exportDirectory,
    String exportName) {}
