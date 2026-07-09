package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cloudsherpa.service.config.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {

  @AfterEach
  void cleanUp() {
    TenantContext.clear();
  }

  @Test
  void getCurrentTenantReturnsNullWhenNothingSet() {
    assertNull(TenantContext.getCurrentTenant());
  }

  @Test
  void setCurrentTenantSetsValueCorrectly() {
    String expectedTenant = "user-uuid-1234";
    TenantContext.setCurrentTenant(expectedTenant);

    assertEquals(expectedTenant, TenantContext.getCurrentTenant());
  }

  @Test
  void clearRemovesTenantFromContext() {
    TenantContext.setCurrentTenant("user-uuid-1234");
    TenantContext.clear();

    assertNull(TenantContext.getCurrentTenant());
  }
}
