package com.cloudsherpa.ingestion.nfr;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.GcpBigQueryNormalizationService;
import com.google.cloud.bigquery.FieldValueList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class BillingRecordIngestionTest {

  @Autowired GcpBigQueryNormalizationService normalizationServie;

  List<FieldValueList> gcpBillingRecords;

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
  void setUp() {}

  @Test
  void smoke() {}

  private List<FieldValueList> generateFieldValueList(Integer numRecords) {

    List<FieldValueList> fieldValueList = new ArrayList<>();

    for (int i = 0; i < numRecords; i++) {}
  }
}
