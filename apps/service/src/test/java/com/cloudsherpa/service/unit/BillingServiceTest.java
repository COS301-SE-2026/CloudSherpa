package com.cloudsherpa.service.unit;

import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.billing.service.BillingService;
import com.cloudsherpa.service.config.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {
  // define a fake user that is logged in
  private static final String TENANT_ID = "123e4567-e89b-12d3-a456-426614174000";

  @Mock private NormalizedCostsRepository normalizedCostsRepository;
  @Mock private CloudAccountRepository cloudAccountRepository;

  @Mock private EntityManager entityManager;
  @Mock private Query query;

  @InjectMocks private BillingService billingService;

  @BeforeEach
  void setUp() {
    // log in as the fake user
    TenantContext.setCurrentTenant(TENANT_ID);

    when(entityManager.createNativeQuery(
            "SET search_path TO tenant_123e4567_e89b_12d3_a456_426614174000, public"))
        .thenReturn(query);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }
}
