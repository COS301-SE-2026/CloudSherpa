package com.cloudsherpa.service.config;

// * Hibernate doesn't know what a logged-in user is.
// * Right before Hibernate runs a database search, it pauses and asks this class:
// * "Wait, whose data am I looking for?" This class simply reads the sticky note
// * and hands the ID over to Hibernate. If the sticky note is blank (like on the
// * login page), it just tells Hibernate to use the public database.

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

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
    // Security measure
    // Every single time a query is executed within an open Session,
    // Hibernate will re-call resolveCurrentTenantIdentifier() and verify that the tenant ID hasn't
    // suddenly changed.
    // Completely prevents having access to other tenants' data
    return true;
  }
}
