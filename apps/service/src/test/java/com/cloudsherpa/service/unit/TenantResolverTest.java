package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.config.TenantResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantResolverTest {

  private final TenantResolver tenantResolver = new TenantResolver();

  @AfterEach
  void cleanUp() {
    TenantContext.clear();
  }

  @Test
  void resolveCurrentTenantIdentifierReturnsTenantIdWhenContextIsSet() {
    String expectedTenantId = "tenant-1234";
    TenantContext.setCurrentTenant(expectedTenantId);

    String actualTenantId = tenantResolver.resolveCurrentTenantIdentifier();

    assertEquals(expectedTenantId, actualTenantId);
  }

  @Test
  void resolveCurrentTenantIdentifierReturnsPublicWhenContextIsNull() {

    String actual = tenantResolver.resolveCurrentTenantIdentifier();

    assertEquals("public", actual);
  }

  @Test
  void validateExistingCurrentSessionsAlwaysReturnsTrue() {
    assertTrue(tenantResolver.validateExistingCurrentSessions());
  }
}
