package com.cloudsherpa.ingestion.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class TenantSchemaService {

  @PersistenceContext private EntityManager entityManager;

  private static final Pattern TENANT_SCHEMA_PATTERN = Pattern.compile("^tenant_[a-f0-9_]{36}$");

  public void useTenantSchema(UUID userId) {
    entityManager
        .createNativeQuery("SET search_path TO " + normalizeTenantSchema(userId) + ", public")
        .executeUpdate();
  }

  public void usePublicSchema() {
    entityManager.createNativeQuery("SET search_path TO public").executeUpdate();
  }

  private String normalizeTenantSchema(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("userId is required");
    }

    String schema = "tenant_" + userId.toString().toLowerCase().replace("-", "_");

    if (!TENANT_SCHEMA_PATTERN.matcher(schema).matches()) {
      throw new IllegalArgumentException("Invalid userId format: " + userId);
    }

    return schema;
  }
}
