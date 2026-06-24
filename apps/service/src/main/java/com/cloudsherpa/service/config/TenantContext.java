package com.cloudsherpa.service.config;

// * Think of this as a temporary sticky note for the server. When a web request
// * comes in, we write the user's ID on this sticky note. This guarantees that
// * while the server is processing things, it never forgets who the user is
// * and never accidentally mixes up their data with another user who is
// * clicking around at the exact same time.

public class TenantContext {

  private TenantContext() {
    throw new IllegalStateException("Utility class");
  }

  private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

  public static String getCurrentTenant() {
    return TENANT.get();
  }

  public static void setCurrentTenant(String tenantId) {
    TENANT.set(tenantId);
  }

  public static void clear() {
    TENANT.remove();
  }
}
