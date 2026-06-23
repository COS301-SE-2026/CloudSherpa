package com.cloudsherpa.service.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

// Hibernate needs a way to figure out which tenant is currently active
// So it uses TenantContext
@Component
public class TenantResolver implements CurrentTenantIdentifierResolver {
  @Override
  public String resolveCurrentTenantIdentifier() {
    String tenantId = TenantContext.getCurrentTenant();

    if (tenantId != null) {
      return tenantId;
    }

    return "public";
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
