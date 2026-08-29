package com.cloudsherpa.service.integration.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloudsherpa.service.analytics.dto.ResourceMetricsGroupDto;
import com.cloudsherpa.service.analytics.model.ResourceMetric;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import com.cloudsherpa.service.config.TenantContext;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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
      "INGESTION_BASE_URL=http://localhost:8081",
      "intelligence-api-key=123"
    })
class NormalizedMetricsServiceIntegrationTest {
  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb:2.16.1-pg16")
                  .asCompatibleSubstituteFor("postgres"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("sherpadb-schema.sql"),
              "/docker-entrypoint-initdb.d/01_schema.sql")
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("historical-metric-seed.sql"),
              "/docker-entrypoint-initdb.d/02_metric_seed.sql");

  @Autowired NormalizedMetricService normalizedMetricService;

  @BeforeEach
  void setUp() {
    TenantContext.setCurrentTenant("5ebe4340-c5ec-4833-ad93-06abf4609f03");
  }

  @Test
  void fetchResourceMetricsShouldCreateResourceMetricsGroupList() {
    UUID resource1Uuid = UUID.fromString("10000000-0000-0000-0000-000000000001");
    UUID resource2Uuid = UUID.fromString("10000000-0000-0000-0000-000000000002");
    UUID resource3Uuid = UUID.fromString("10000000-0000-0000-0000-000000000003");

    List<ResourceMetricsGroupDto> expected =
        List.of(
            new ResourceMetricsGroupDto(
                resource1Uuid, List.of(new ResourceMetric("CPUUtilization", "cpu"))),
            new ResourceMetricsGroupDto(
                resource2Uuid, List.of(new ResourceMetric("NetworkIn", "network"))),
            new ResourceMetricsGroupDto(
                resource3Uuid,
                List.of(
                    new ResourceMetric("NetworkOut", "network"),
                    new ResourceMetric("DiskReadBytes", "disk"),
                    new ResourceMetric("DiskWriteBytes", "disk"))));

    List<ResourceMetricsGroupDto> actual = normalizedMetricService.fetchResourceMetrics();

    assertEquals(expected.size(), actual.size());
    assertEquals(sortedResourceMetricsGroups(expected), sortedResourceMetricsGroups(actual));
  }

  private List<ResourceMetricsGroupDto> sortedResourceMetricsGroups(
      List<ResourceMetricsGroupDto> groups) {
    return groups.stream()
        .map(
            group ->
                new ResourceMetricsGroupDto(
                    group.resourceId(),
                    group.metrics().stream()
                        .sorted(
                            Comparator.comparing(ResourceMetric::metricName)
                                .thenComparing(ResourceMetric::metricType))
                        .toList()))
        .sorted(Comparator.comparing(ResourceMetricsGroupDto::resourceId))
        .toList();
  }

  @AfterEach
  void clearTenant() {
    TenantContext.clear();
  }
}
