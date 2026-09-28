package com.cloudsherpa.ingestion.nfr;

import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.AzureBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "AES_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "org.jobrunr.background-job-server.enabled=false",
      "org.jobrunr.dashboard.enabled=false"
    })
class AzureBillingRecordIngestionTest {

  private static final UUID TENANT_ID = UUID.fromString("a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246");
  private static final UUID ACCOUNT_ID = UUID.fromString("a0000000-0000-0000-0000-000000000003");
  private static final UUID CONFIG_ID = UUID.fromString("c181db1b-e20b-4b34-a606-af13a2d48524");
  private static final UUID CREDENTIAL_ID = UUID.fromString("94df7256-4036-4f56-bc03-74fe93158ee7");
  private static final String STORAGE_ACCOUNT_NAME = "nfrstorageaccount";
  private static final String STORAGE_CONTAINER = "billing-exports";
  private static final String BILLING_EXPORT_DIRECTORY = "exports/daily";
  private static final String BILLING_EXPORT_NAME = "nfr-billing-export";

  @Autowired BillingExportConfigRepository exportConfigRepository;
  @Autowired AzureBillingExportConfigRepository azureExportConfigRepository;
  @Autowired CloudCredentialRepository cloudCredentialRepository;
  @Autowired CredentialEncryptionService credentialEncryptionService;

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb-ha:pg16-ts2.29")
                  .asCompatibleSubstituteFor("postgres"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("sherpadb-schema.sql"),
              "/docker-entrypoint-initdb.d/01_schema.sql")
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("nfr-user.sql"),
              "/docker-entrypoint-initdb.d/02_nfr_user.sql");

  @BeforeEach
  void setUp() {
    persistConfigs();
    writeCredentials();
  }

  private void persistConfigs() {
    BillingExportConfig config =
        new BillingExportConfig(
            CONFIG_ID, ACCOUNT_ID, OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
    exportConfigRepository.save(config);

    AzureBillingExportConfig azureConfig =
        new AzureBillingExportConfig(
            CONFIG_ID,
            STORAGE_ACCOUNT_NAME,
            STORAGE_CONTAINER,
            BILLING_EXPORT_DIRECTORY,
            BILLING_EXPORT_NAME);
    azureExportConfigRepository.save(azureConfig);
  }

  private void writeCredentials() {
    String credentialJson =
        """
            {
              "subscriptionId": "00000000-0000-0000-0000-000000000010",
              "tenantId": "00000000-0000-0000-0000-000000000011",
              "clientId": "00000000-0000-0000-0000-000000000012",
              "clientSecret": "test-client-secret"
            }
            """;

    CloudCredential credential =
        new CloudCredential(
            CREDENTIAL_ID,
            ACCOUNT_ID,
            "AZURE",
            "oauth",
            credentialEncryptionService.encrypt(credentialJson),
            OffsetDateTime.now());
    cloudCredentialRepository.save(credential);
  }
}
