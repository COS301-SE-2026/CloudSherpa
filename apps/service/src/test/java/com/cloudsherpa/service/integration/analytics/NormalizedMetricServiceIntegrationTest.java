package com.cloudsherpa.service.integration.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.service.analytics.dto.DownsampledSeriesRequestDto;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import com.cloudsherpa.service.config.TenantContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
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
      "INGESTION_BASE_URL=http://localhost:8081",
      "intelligence-api-key=123"
    })
class NormalizedMetricsServiceIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb-ha:pg16-ts2.29")
                  .asCompatibleSubstituteFor("postgres"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("sherpadb-schema.sql"),
              "/docker-entrypoint-initdb.d/01_schema.sql")
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("historical-metric-seed.sql"),
              "/docker-entrypoint-initdb.d/02_metric_seed.sql");

  @Autowired NormalizedMetricService normalizedMetricService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    TenantContext.setCurrentTenant("5ebe4340-c5ec-4833-ad93-06abf4609f03");
  }

  @Test
  void fetchDownsampledSeriesReturnsAllPointsWhenBelowDownsamplingThreshold() {

    seedHistoricalMetrics(100);

    DownsampledSeriesRequestDto request =
        new DownsampledSeriesRequestDto(
            UUID.fromString("10000000-0000-0000-0000-000000000001"),
            "CPUUtilization",
            Instant.now().minusSeconds(60 * 60 * 72),
            Instant.now());

    List<NormalizedMetrics> downsampledSeries =
        normalizedMetricService.fetchDownsampledSeries(request);

    assertEquals(100, downsampledSeries.size());

    metricsCleanup();
  }

  @Test
  void fetchDownsampledSeriesReturnsDownsampledPointsWhenAboveThreshold() {
    seedHistoricalMetrics(500);

    DownsampledSeriesRequestDto request =
        new DownsampledSeriesRequestDto(
            UUID.fromString("10000000-0000-0000-0000-000000000001"),
            "CPUUtilization",
            Instant.now().minusSeconds(60 * 60 * 72),
            Instant.now());

    List<NormalizedMetrics> downsampledSeries =
        normalizedMetricService.fetchDownsampledSeries(request);

    assertEquals(300, downsampledSeries.size());

    metricsCleanup();
  }

  @AfterEach
  void clearTenant() {
    TenantContext.clear();
  }

  private void seedHistoricalMetrics(Integer numDataPoints) {
    jdbcTemplate.queryForObject("SELECT seed_normalized_metrics(?)", Object.class, numDataPoints);
  }

  private void metricsCleanup() {
    jdbcTemplate.execute(
        "TRUNCATE TABLE tenant_5ebe4340_c5ec_4833_ad93_06abf4609f03.normalized_metrics");
  }
}
