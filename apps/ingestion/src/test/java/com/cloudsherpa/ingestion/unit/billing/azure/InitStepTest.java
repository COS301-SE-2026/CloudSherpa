package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.billing.BillingExportConfigService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.InitStep;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InitStepTest {

  @Mock CloudCredentialRepository cloudCredentialRepository;
  @Mock CredentialEncryptionService credentialEncryptionService;
  @Mock BillingExportConfigService billingExportConfigService;

  @InjectMocks InitStep initStep;

  @Test
  void initStepShouldLoadValidCredentials() {
    mockValidSetCredentials();
    mockValidSetExportConfig();

    AzureBillingContext validContext = validBillingContext();

    initStep.execute(validContext);

    assertEquals(
        "00000000-0000-0000-0000-000000000010", validContext.getCredentials().getSubscriptionId());
    assertEquals(
        "00000000-0000-0000-0000-000000000011", validContext.getCredentials().getTenantId());
    assertEquals(
        "00000000-0000-0000-0000-000000000012", validContext.getCredentials().getClientId());
    assertEquals("test-client-secret", validContext.getCredentials().getClientSecret());
  }

  @Test
  void initStepShouldLoadValidExportConfig() {
    mockValidSetCredentials();
    mockValidSetExportConfig();

    AzureBillingContext validContext = validBillingContext();

    initStep.execute(validContext);

    assertEquals("teststorageaccount", validContext.getExportConfig().getStorageAccountName());
    assertEquals(
        UUID.fromString("00000000-0000-0000-0000-000000000001"),
        validContext.getExportConfig().getConfigId());
    assertEquals("billing-exports", validContext.getExportConfig().getStorageContainer());
    assertEquals("exports/daily", validContext.getExportConfig().getBillingExportDirectory());
    assertEquals("test-billing-export", validContext.getExportConfig().getBillingExportName());
  }

  private AzureBillingContext validBillingContext() {
    return new AzureBillingContext(
        UUID.fromString("00000000-0000-0000-0000-000000000004"),
        UUID.fromString("00000000-0000-0000-0000-000000000001"));
  }

  private void mockValidSetCredentials() {
    BillingExportConfig validConfig = getValidBillingExportConfig();

    when(billingExportConfigService.getBillingExportConfig(validConfig.getId()))
        .thenReturn(validConfig);

    List<CloudCredential> validCredentialsRepo =
        List.of(
            new CloudCredential(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                validConfig.getAccountId(),
                "AZURE",
                "SERVICE_PRINCIPAL",
                "encrypted-test-credentials",
                OffsetDateTime.parse("2026-01-01T10:00:00Z")));

    when(cloudCredentialRepository.findByAccountIdAndProvider(validConfig.getAccountId(), "AZURE"))
        .thenReturn(validCredentialsRepo);

    String decryptedCredentialsJson =
        """
      {
        "subscriptionId": "00000000-0000-0000-0000-000000000010",
        "tenantId": "00000000-0000-0000-0000-000000000011",
        "clientId": "00000000-0000-0000-0000-000000000012",
        "clientSecret": "test-client-secret"
      }
      """;

    when(credentialEncryptionService.decrypt("encrypted-test-credentials"))
        .thenReturn(decryptedCredentialsJson);
  }

  private void mockValidSetExportConfig() {
    AzureBillingExportConfig validExportConfig = getValidAzureBillingExportConfig();
    when(billingExportConfigService.getAzureBillingExportConfig(validExportConfig.getConfigId()))
        .thenReturn(validExportConfig);
  }

  private BillingExportConfig getValidBillingExportConfig() {
    return new BillingExportConfig(
        UUID.fromString("00000000-0000-0000-0000-000000000001"),
        UUID.fromString("00000000-0000-0000-0000-000000000002"),
        OffsetDateTime.parse("2026-01-01T10:00:00Z"));
  }

  private AzureBillingExportConfig getValidAzureBillingExportConfig() {
    return new AzureBillingExportConfig(
        UUID.fromString("00000000-0000-0000-0000-000000000001"),
        "teststorageaccount",
        "billing-exports",
        "exports/daily",
        "test-billing-export");
  }
}
