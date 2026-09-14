package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.normalization.AzureBillingNormalizer;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureBillingNormalizerTest {

  private static final UUID EXECUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final UUID CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private static final String RESOURCE_ID =
      "/subscriptions/test-subscription/resourceGroups/test-rg/providers/Microsoft.Storage/storageAccounts/teststorage";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private AzureBillingNormalizer normalizer;

  @BeforeEach
  void setUp() {
    this.normalizer = new AzureBillingNormalizer(objectMapper);
  }

  @Test
  void shouldNormalizeUsageRowWithDeduplicatedServiceNameAndUtcDayBoundaries()
      throws JsonProcessingException, NoSuchAlgorithmException {
    RawBillingRow billingRow = billingRow();
    BillingExportExecution execution = billingExportExecution();
    NormalizedCosts actual = normalizer.normalize(billingRow, execution);

    assertNormalizedCostsEqual(expectedCosts(), actual);
  }

  private void assertNormalizedCostsEqual(NormalizedCosts expected, NormalizedCosts actual) {
    assertNotNull(expected, "Expected normalized costs");
    assertNotNull(actual, "Actual normalized costs");
    assertAll(
        "Normalized costs",
        () -> assertEquals(expected.getCostId(), actual.getCostId(), "costId"),
        () -> assertEquals(expected.getExecutionId(), actual.getExecutionId(), "executionId"),
        () -> assertEquals(expected.getExecution(), actual.getExecution(), "execution"),
        () -> assertEquals(expected.getChargeId(), actual.getChargeId(), "chargeId"),
        () -> assertEquals(expected.getResourceId(), actual.getResourceId(), "resourceId"),
        () -> assertEquals(expected.getProvider(), actual.getProvider(), "provider"),
        () ->
            assertEquals(
                expected.getBillingAccountId(), actual.getBillingAccountId(), "billingAccountId"),
        () -> assertEquals(expected.getChargeType(), actual.getChargeType(), "chargeType"),
        () -> assertEquals(expected.getServiceName(), actual.getServiceName(), "serviceName"),
        () -> assertEquals(expected.getCostAmount(), actual.getCostAmount(), "costAmount"),
        () -> assertEquals(expected.getCurrency(), actual.getCurrency(), "currency"),
        () ->
            assertEquals(
                expected.getUsageStartTime(), actual.getUsageStartTime(), "usageStartTime"),
        () -> assertEquals(expected.getUsageEndTime(), actual.getUsageEndTime(), "usageEndTime"),
        () -> assertEquals(expected.getMetadata(), actual.getMetadata(), "metadata"));
  }

  private NormalizedCosts expectedCosts() throws JsonProcessingException, NoSuchAlgorithmException {
    NormalizedCosts expected = new NormalizedCosts();
    expected.setCostId(
        HexFormat.of()
            .formatHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(objectMapper.writeValueAsBytes(billingRow()))));
    expected.setExecutionId(EXECUTION_ID);
    expected.setChargeId(RESOURCE_ID + "%%%Microsoft Storage Blob");
    expected.setResourceId(RESOURCE_ID);
    expected.setProvider(ProviderEnum.AZURE);
    expected.setBillingAccountId("billing-account-1");
    expected.setChargeType(ChargeTypeEnum.Usage);
    expected.setServiceName("Microsoft Storage Blob");
    expected.setCostAmount(new BigDecimal("12.50"));
    expected.setUsageStartTime(OffsetDateTime.parse("2026-09-01T00:00:00Z"));
    expected.setUsageEndTime(OffsetDateTime.parse("2026-09-01T23:59:59.999999999Z"));
    return expected;
  }

  private RawBillingRow billingRow() {
    return new RawBillingRow(
        "billing-account-1",
        LocalDate.of(2026, 9, 1),
        "Microsoft.Storage",
        "storage",
        "Blob Storage",
        RESOURCE_ID,
        "Usage",
        "USD",
        new BigDecimal("12.50"));
  }

  private BillingExportExecution billingExportExecution() {
    return new BillingExportExecution(EXECUTION_ID, CONFIG_ID, ExecutionStatusEnum.processing);
  }
}
