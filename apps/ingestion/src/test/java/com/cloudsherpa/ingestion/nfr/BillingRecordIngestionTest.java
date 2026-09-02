package com.cloudsherpa.ingestion.nfr;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.GcpBillingIngestionService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.GcpBigQueryNormalizationService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingQueryStep;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBilllingDiscoveryStep;
import com.cloudsherpa.utils.GcpFieldValueListTestUtil;
import com.google.cloud.bigquery.FieldValueList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
class BillingRecordIngestionTest {

  @Autowired GcpBigQueryNormalizationService normalizationServie;
  @Autowired GcpBillingIngestionService ingestionService;
  @MockitoBean GcpBilllingDiscoveryStep discoveryStep;
  @MockitoBean GcpBillingQueryStep queryStep;

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

  @BeforeEach()
  void setUp() {
    mockSuccessfulDiscovery();
  }

  @Test
  void smoke() {
    // Arrange
    List<FieldValueList> gcpBillingRecords = generateFieldValueList(10);

    mockSuccessfulQuery(gcpBillingRecords);

    ingestionService.execute(
        "a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246", "bce4f71d-7b9d-4ab3-a99c-5d3f7511c388");
  }

  private List<FieldValueList> generateFieldValueList(Integer numRecords) {

    List<FieldValueList> fieldValueList = new ArrayList<>();

    long start = 1786442400;

    for (int i = 0; i < numRecords; i++) {
      String timestampStart = Long.toString(start + i) + ".000000";
      String timestampEnd = Long.toString(start + i + 1) + ".000000";

      fieldValueList.addLast(
          GcpFieldValueListTestUtil.rowWithNullResourceName(timestampStart, timestampEnd));
    }

    return fieldValueList;
  }

  private void mockSuccessfulDiscovery() {
    doAnswer(
            invocation -> {
              GcpBillingContext context = invocation.getArgument(0);
              context.setBillingExportTableIdentifier(
                  "gcp_billing_export_resource_v1_000000_000000_000000");
              context.setFullyQualifiedExportTableIdentifier(
                  "nfr-project.nfr-dataset.gcp_billing_export_resource_v1_000000_000000_000000");
              return null;
            })
        .when(discoveryStep)
        .execute(any(GcpBillingContext.class));
  }

  private void mockSuccessfulQuery(List<FieldValueList> list) {
    doAnswer(
            invocation -> {
              GcpBillingContext context = invocation.getArgument(0);
              context.setTableResult(GcpFieldValueListTestUtil.tableResult(list));

              return null;
            })
        .when(queryStep)
        .execute(any(GcpBillingContext.class));
  }
}
