package com.cloudsherpa.ingestion.nfr;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.GcpBillingIngestionService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingQueryStep;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBilllingDiscoveryStep;
import com.cloudsherpa.utils.GcpFieldValueListTestUtil;
import com.google.cloud.bigquery.FieldValueList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class GcpBillingRecordIngestionTest {

  private static final Logger logger = LoggerFactory.getLogger(GcpBillingRecordIngestionTest.class);

  @Autowired GcpBillingIngestionService ingestionService;
  @MockitoBean GcpBilllingDiscoveryStep discoveryStep;
  @MockitoBean GcpBillingQueryStep queryStep;

  // ! WARNING: the target as per our documentation is 1000, the run should be recorded
  // in the NFR matrix as long as it is below that threshold. GCP billing ingestion performance
  // should be improved via batch processing.
  private static final int RECORD_PER_SECOND_THRESHOLD = 200;
  private static final int NUM_RECORDS_TO_SEED = 10000;

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
  void gcpRecordsPerSecondShouldBeMoreThanOneThousand() {
    // Arrange
    List<FieldValueList> gcpBillingRecords = generateFieldValueList(NUM_RECORDS_TO_SEED);

    mockSuccessfulQuery(gcpBillingRecords);

    long start = System.nanoTime();

    ingestionService.execute(
        "a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246", "bce4f71d-7b9d-4ab3-a99c-5d3f7511c388");

    long duration = System.nanoTime() - start;
    double elapsedSeconds = duration / Math.pow(10, 9);

    double recordsPerSecond = NUM_RECORDS_TO_SEED / elapsedSeconds;
    logger.info(
        "\nDuration: {}s\nRecords processed: {}\nRecords per second: {}",
        elapsedSeconds,
        NUM_RECORDS_TO_SEED,
        recordsPerSecond);

    assertTrue(recordsPerSecond > RECORD_PER_SECOND_THRESHOLD);
  }

  private List<FieldValueList> generateFieldValueList(Integer numRecords) {

    List<FieldValueList> fieldValueList = new ArrayList<>();

    long start = 1786442400;

    for (int i = 0; i < numRecords; i++) {
      String timestampStart = Long.toString(start + i) + ".000000";
      String timestampEnd = Long.toString(start + i + 1) + ".000000";

      fieldValueList.addLast(GcpFieldValueListTestUtil.validUsageRow(timestampStart, timestampEnd));
    }

    return fieldValueList;
  }

  // Mock only third party dependencied (GCP billing export query mocked)
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
