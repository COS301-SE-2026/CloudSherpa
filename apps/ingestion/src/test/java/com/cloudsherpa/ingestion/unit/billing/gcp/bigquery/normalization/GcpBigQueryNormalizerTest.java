package com.cloudsherpa.ingestion.unit.billing.gcp.bigquery.normalization;

import static com.cloudsherpa.utils.GcpFieldValueListTestUtil.rowWithNullResourceName;
import static com.cloudsherpa.utils.GcpFieldValueListTestUtil.validUsageRow;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.ChargeIdNormalizer;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.CreditProcessingState;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.GcpBigQueryBillingRecord;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.GcpBigQueryNormalizer;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.ServiceNameNormalizer;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GcpBigQueryNormalizerTest {

  private static final String BILLING_ID = "billing-account-1";

  @Test
  void shouldCreateNormalizedCost() {
    // arrange
    GcpBigQueryNormalizer normalizer = newNormalizer();
    normalizer.setBillingId(BILLING_ID);
    NormalizedCosts expected = expectedNormalizedCosts();

    // act
    NormalizedCosts actual = normalizer.normalize(validNonCreditRecord(), validBillingExport());

    // assert
    assertAll(
        () -> assertEquals(expected.getCostId(), actual.getCostId()),
        () -> assertEquals(expected.getExecutionId(), actual.getExecutionId()),
        () -> assertEquals(expected.getChargeId(), actual.getChargeId()),
        () -> assertEquals(expected.getResourceId(), actual.getResourceId()),
        () -> assertEquals(expected.getProvider(), actual.getProvider()),
        () -> assertEquals(expected.getBillingAccountId(), actual.getBillingAccountId()),
        () -> assertEquals(expected.getChargeType(), actual.getChargeType()),
        () -> assertEquals(expected.getServiceName(), actual.getServiceName()),
        () -> assertEquals(expected.getCostAmount(), actual.getCostAmount()),
        () -> assertEquals(expected.getUsageStartTime(), actual.getUsageStartTime()),
        () -> assertEquals(expected.getUsageEndTime(), actual.getUsageEndTime()));
  }

  @Test
  void shouldThrowWhenBillingIdNotSet() {
    // Arrange
    GcpBigQueryNormalizer normalizer = newNormalizer();
    GcpBigQueryBillingRecord gcpRecord = validNonCreditRecord();

    // Act & assert
    assertThatThrownBy(() -> normalizer.getBillingAccountId(gcpRecord))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldReturnNoResourceIdWhenResourceNameIsNull() {
    // arrange
    GcpBigQueryNormalizer normalizer = newNormalizer();

    // act
    String resourceId =
        normalizer.getResourceId(
            new GcpBigQueryBillingRecord(rowWithNullResourceName(), new CreditProcessingState()));

    // assert
    assertEquals("NoResourceId", resourceId);
  }

  private GcpBigQueryBillingRecord validNonCreditRecord() {
    return new GcpBigQueryBillingRecord(validUsageRow(), new CreditProcessingState());
  }

  private NormalizedCosts expectedNormalizedCosts() {
    NormalizedCosts expected = new NormalizedCosts();
    expected.setCostId(
        "GCP%%%billing-account-1%%%project-1%%%service-1%%%sku-1%%%"
            + "//compute.googleapis.com/projects/project-1/zones/us/vm-1%%%Usage");
    expected.setExecutionId(UUID.fromString("e95b9649-9df5-4353-add3-002638de271f"));
    expected.setChargeId(
        "//compute.googleapis.com/projects/project-1/zones/us/vm-1%%%"
            + "Compute Engine N1 Predefined Instance Core");
    expected.setResourceId("//compute.googleapis.com/projects/project-1/zones/us/vm-1");
    expected.setProvider(ProviderEnum.GCP);
    expected.setBillingAccountId(BILLING_ID);
    expected.setChargeType(ChargeTypeEnum.Usage);
    expected.setServiceName("Compute Engine N1 Predefined Instance Core");
    expected.setCostAmount(new BigDecimal("12.34"));
    expected.setUsageStartTime(OffsetDateTime.of(2026, 8, 11, 10, 0, 0, 0, ZoneOffset.UTC));
    expected.setUsageEndTime(OffsetDateTime.of(2026, 8, 11, 11, 0, 0, 0, ZoneOffset.UTC));
    return expected;
  }

  private BillingExport validBillingExport() {
    return new BillingExport(
        "e95b9649-9df5-4353-add3-002638de271f", "b7d1a897-66da-43e6-b499-30e53ce59207", List.of());
  }

  private GcpBigQueryNormalizer newNormalizer() {
    return new GcpBigQueryNormalizer(new ChargeIdNormalizer(new ServiceNameNormalizer()));
  }
}
