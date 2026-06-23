package com.cloudsherpa.service.config;

// SpringBoot handles simultaneous traffic by assigning each incoming web request to its own
// dedicated "Thread"

public class TenantContext {
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
