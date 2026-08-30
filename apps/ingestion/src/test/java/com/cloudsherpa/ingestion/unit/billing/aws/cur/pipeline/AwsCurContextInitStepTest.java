package com.cloudsherpa.ingestion.unit.billing.aws.cur.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.billing.BillingExportConfigService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContext;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContextInitStep;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.AwsBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.regions.Region;

@ExtendWith(MockitoExtension.class)
class AwsCurContextInitStepTest {

  @Mock BillingExportExecutionRepository billingExportExecutionRepository;

  @Mock BillingExportConfigService billingExportConfigService;

  @Mock CloudCredentialRepository cloudCredentialRepository;

  @Mock CredentialEncryptionService encryptionService;

  @TempDir Path tempDir;

  UUID configUuid;
  UUID accountId;
  String userId;
  String configId;
  UUID validBillingExportId;
  AwsCurContext context;
  AwsCurContextInitStep step;
  BillingExportConfig validConfig;
  AwsBillingExportConfig validAwsConfig;
  CloudCredential validCredential;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID().toString();

    configUuid = UUID.randomUUID();
    configId = configUuid.toString();

    accountId = UUID.randomUUID();
    validBillingExportId = UUID.randomUUID();

    context = new AwsCurContext(userId, configId);

    validConfig = new BillingExportConfig(configUuid, accountId, OffsetDateTime.now());

    validAwsConfig =
        new AwsBillingExportConfig(
            configUuid, " test-bucket ", "eu-north-1", "cur-prefix", "cur-export");

    String credentialJson =
        """
        {
          "accessKeyId": "test-access-key",
          "secretAccessKey": "test-secret-key"
        }
        """;

    validCredential =
        new CloudCredential(
            UUID.randomUUID(),
            accountId,
            "AWS",
            "access-key",
            credentialJson,
            OffsetDateTime.now());

    step =
        new AwsCurContextInitStep(
            tempDir.toString(),
            billingExportExecutionRepository,
            billingExportConfigService,
            cloudCredentialRepository,
            encryptionService);
  }

  @Test
  void executeShouldAddPreviouslyProcessedExportsToContext() {
    // When execute() runs, previously processed billing exports from the repo are
    // added to the context

    // Arrange
    mockValidConfigAndCredential();
    mockProcessedExport(validBillingExportId);

    // Act
    step.execute(context);

    // Assert
    assertEquals(List.of(validBillingExportId.toString()), context.getProcessedExports());
  }

  @Test
  void executeShouldCopyBillingExportConfigOntoContext() {
    // Extract and copy billing export config onto context

    // Arrange
    mockValidConfigAndCredential();
    mockProcessedExport(validBillingExportId);

    // act
    step.execute(context);

    // assert
    assertEquals("test-bucket", context.getBucketName());
    assertEquals(Region.EU_NORTH_1, context.getBucketRegion());
    assertEquals("cur-prefix", context.getExportPrefix());
    assertEquals("cur-export", context.getExportName());
    assertEquals(accountId, context.getAccountId());
  }

  @Test
  void executeShouldCreateAndSetAwsCurTempDirectory() {
    // arange
    mockValidConfigAndCredential();
    mockProcessedExport(validBillingExportId);

    // act
    step.execute(context);

    // assert
    assertEquals(tempDir, context.getAwsCurTmpDir());
  }

  @Test
  void executeShouldSetAwsCredentialsOnContext() {
    // arange
    mockValidConfigAndCredential();
    mockProcessedExport(validBillingExportId);

    // act
    step.execute(context);

    // assert
    assertEquals("test-access-key", context.getCredentials().getAccessKeyId());
    assertEquals("test-secret-key", context.getCredentials().getSecretAccessKey());
  }

  @Test
  void executeShouldThrowWhenNoAwsCredentialsForAccountId() {
    // arrange
    when(billingExportConfigService.getBillingExportConfig(configUuid)).thenReturn(validConfig);
    when(billingExportConfigService.getAccountAwsBillingExportConfig(configUuid))
        .thenReturn(validAwsConfig);

    mockProcessedExport(validBillingExportId);

    // No AWS Credentials for account
    when(cloudCredentialRepository.findByAccountIdAndProvider(accountId, "AWS"))
        .thenReturn(List.of());

    // act & assert
    assertThrows(IllegalStateException.class, () -> step.execute(context));
  }

  @Test
  void executeShouldThrowWhenCredentialsInvalidJsonEncoding() {
    // arrange
    when(billingExportConfigService.getBillingExportConfig(configUuid)).thenReturn(validConfig);
    when(billingExportConfigService.getAccountAwsBillingExportConfig(configUuid))
        .thenReturn(validAwsConfig);
    mockProcessedExport(validBillingExportId);

    String credentialInvalidJson =
        """
          accessKeyId": "test-access-key",
          "secretAccessKey": "test-secret-key"
        """;

    CloudCredential invalidCredential =
        new CloudCredential(
            UUID.randomUUID(),
            accountId,
            "AWS",
            "access-key",
            credentialInvalidJson,
            OffsetDateTime.now());

    when(cloudCredentialRepository.findByAccountIdAndProvider(accountId, "AWS"))
        .thenReturn(List.of(invalidCredential));

    when(encryptionService.decrypt(invalidCredential.getCredentialValue()))
        .thenReturn(invalidCredential.getCredentialValue());

    // act & assert
    assertThrows(IllegalStateException.class, () -> step.execute(context));
  }

  private void mockValidConfigAndCredential() {
    when(billingExportConfigService.getBillingExportConfig(configUuid)).thenReturn(validConfig);
    when(billingExportConfigService.getAccountAwsBillingExportConfig(configUuid))
        .thenReturn(validAwsConfig);

    when(cloudCredentialRepository.findByAccountIdAndProvider(accountId, "AWS"))
        .thenReturn(List.of(validCredential));

    when(encryptionService.decrypt(validCredential.getCredentialValue()))
        .thenReturn(validCredential.getCredentialValue());
  }

  private void mockProcessedExport(UUID billingExportId) {
    BillingExportExecution billingExportExecution =
        new BillingExportExecution(billingExportId, configUuid, ExecutionStatusEnum.completed);

    when(billingExportExecutionRepository.findAll()).thenReturn(List.of(billingExportExecution));
  }
}
