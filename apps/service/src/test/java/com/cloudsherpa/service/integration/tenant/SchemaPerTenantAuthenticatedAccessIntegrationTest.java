package com.cloudsherpa.service.integration.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.config.TenantContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "AES_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "INGESTION_BASE_URL=http://localhost:8081",
      "intelligence-api-key=test-key",
      "ingestion-api-key=test-key"
    })
class SchemaPerTenantAuthenticatedAccessIntegrationTest {

  private static final String JWT_SECRET = "mock_secret_32_chars_long_value_123456";

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb-ha:pg16-ts2.29")
                  .asCompatibleSubstituteFor("postgres"))
          .withInitScript("sherpadb-schema.sql");

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private ResourceRepository resourceRepository;

  private UUID tenantOneId;
  private UUID tenantTwoId;
  private UUID tenantOneAccountId;
  private UUID tenantTwoAccountId;

  @BeforeEach
  void setUp() {
    tenantOneId = UUID.randomUUID();
    tenantTwoId = UUID.randomUUID();

    tenantOneAccountId = createTenantWithAccount(tenantOneId);
    tenantTwoAccountId = createTenantWithAccount(tenantTwoId);
  }

  @AfterEach
  void clearTenant() {
    TenantContext.clear();
  }

  @Test
  void unauthenticatedRequestIsRejected() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/analytics/resource-names", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void authenticatedTenantCanReadItsOwnDataAndNeverAnotherTenantsData() {
    createResource(tenantOneId, tenantOneAccountId, UUID.randomUUID(), "tenant-one-resource");
    createResource(tenantTwoId, tenantTwoAccountId, UUID.randomUUID(), "tenant-two-resource");

    ResponseEntity<String> tenantOneResponse = getResourceNamesWithToken(createToken(tenantOneId));
    ResponseEntity<String> tenantTwoResponse = getResourceNamesWithToken(createToken(tenantTwoId));

    assertThat(tenantOneResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(tenantOneResponse.getBody()).contains("tenant-one-resource");

    assertThat(tenantOneResponse.getBody()).doesNotContain("tenant-two-resource");

    assertThat(tenantTwoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(tenantTwoResponse.getBody()).contains("tenant-two-resource");
    assertThat(tenantTwoResponse.getBody()).doesNotContain("tenant-one-resource");
  }

  @Test
  void creatingResourceWritesOnlyToAuthenticatedTenantsSchema() {
    UUID resourceId =
        createResource(tenantOneId, tenantOneAccountId, UUID.randomUUID(), "created-by-tenant-one");

    assertThat(resourceExistsInSchema(tenantOneId, resourceId)).isTrue();

    assertThat(resourceExistsInSchema(tenantTwoId, resourceId)).isFalse();
  }

  @Test
  void updatingResourceChangesOnlyTheCorrectTenantRecord() {
    UUID sharedResourceId = UUID.randomUUID();

    createResource(tenantOneId, tenantOneAccountId, sharedResourceId, "tenant-one-original");
    createResource(tenantTwoId, tenantTwoAccountId, sharedResourceId, "tenant-two-original");

    runAsTenant(
        tenantOneId,
        () -> {
          Resource tenantOneResource = resourceRepository.findById(sharedResourceId).orElseThrow();

          tenantOneResource.setStatus(StatusEnum.disabled);
          resourceRepository.saveAndFlush(tenantOneResource);
        });

    assertThat(resourceStatusInSchema(tenantOneId, sharedResourceId)).isEqualTo("disabled");
    assertThat(resourceStatusInSchema(tenantTwoId, sharedResourceId)).isEqualTo("active");
  }

  @Test
  void deletingResourceDeletesOnlyTheCorrectTenantRecord() {
    UUID sharedResourceId = UUID.randomUUID();

    createResource(tenantOneId, tenantOneAccountId, sharedResourceId, "tenant-one-delete");
    createResource(tenantTwoId, tenantTwoAccountId, sharedResourceId, "tenant-two-keep");

    runAsTenant(tenantOneId, () -> resourceRepository.deleteById(sharedResourceId));

    assertThat(resourceExistsInSchema(tenantOneId, sharedResourceId)).isFalse();
    assertThat(resourceExistsInSchema(tenantTwoId, sharedResourceId)).isTrue();
  }

  private ResponseEntity<String> getResourceNamesWithToken(String token) {
    HttpHeaders headers = new HttpHeaders();

    headers.add(HttpHeaders.COOKIE, "auth_token=" + token);

    return restTemplate.exchange(
        "/analytics/resource-names", HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  private UUID createTenantWithAccount(UUID tenantId) {
    UUID connectionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    jdbcTemplate.update(
        """
        INSERT INTO public.users (user_id, email, username, password_hash)
        VALUES (?, ?, ?, ?)
        """,
        tenantId,
        tenantId + "@example.test",
        "user-" + tenantId,
        "not-used-by-this-test");

    jdbcTemplate.query("SELECT public.create_new_tenant(?)", resultset -> {}, tenantId);

    jdbcTemplate.update(
        """
        INSERT INTO public.cloud_connection (connection_id, user_id, provider, status)
        VALUES (?, ?, 'AWS', 'active')
        """,
        connectionId,
        tenantId);

    jdbcTemplate.update(
        """
        INSERT INTO public.cloud_account (account_id, connection_id, account_type, ingestion_period)
        VALUES (?, ?, 'aws_account', '1m')
        """,
        accountId,
        connectionId);

    return accountId;
  }

  private UUID createResource(UUID tenantId, UUID accountId, UUID resourceId, String resourceName) {

    Resource resource =
        new Resource.Builder()
            .id(resourceId)
            .accountId(accountId)
            .resourceType("AWS/EC2")
            .resourceName(resourceName)
            .resourceIdentifier("i-" + resourceId)
            .resourceIdentifierType("InstanceId")
            .region("us-east-1")
            .status(StatusEnum.active)
            .tags(Map.of())
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
            .lastUpdated(OffsetDateTime.now(ZoneOffset.UTC))
            .build();

    runAsTenant(tenantId, () -> resourceRepository.saveAndFlush(resource));

    return resourceId;
  }

  private boolean resourceExistsInSchema(UUID tenantId, UUID resourceId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM "
                + tenantSchemaName(tenantId)
                + ".resource WHERE resource_id = ?",
            Integer.class,
            resourceId);

    return count != null && count == 1;
  }

  private String resourceStatusInSchema(UUID tenantId, UUID resourceId) {
    return jdbcTemplate.queryForObject(
        "SELECT status::text FROM "
            + tenantSchemaName(tenantId)
            + ".resource WHERE resource_id = ?",
        String.class,
        resourceId);
  }

  private void runAsTenant(UUID tenantId, Runnable action) {
    TenantContext.setCurrentTenant(tenantId.toString());

    try {
      action.run();
    } finally {
      TenantContext.clear();
    }
  }

  private String createToken(UUID tenantId) {
    SecretKey signingKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    Instant now = Instant.now();

    return Jwts.builder()
        .setSubject(tenantId.toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(300)))
        .signWith(signingKey)
        .compact();
  }

  private String tenantSchemaName(UUID tenantId) {
    return "tenant_" + tenantId.toString().replace("-", "_");
  }
}
