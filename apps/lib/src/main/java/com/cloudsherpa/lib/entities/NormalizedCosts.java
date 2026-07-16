package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name = "normalized_costs")
public class NormalizedCosts {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "cost_id", nullable = false, updatable = false)
  private UUID costId;

  @Column(name = "resource_id")
  private UUID resourceId;

  @ManyToOne
  @JoinColumn(name = "resource_id", insertable = false, updatable = false)
  private Resource resource;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "provider", nullable = false, columnDefinition = "public.provider_enum")
  private ProviderEnum provider;

  @Column(name = "billing_account_id")
  private String billingAccountId;

  @Column(name = "service_name", nullable = false)
  private String serviceName;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "charge_type", nullable = false, columnDefinition = "public.charge_type_enum")
  private ChargeTypeEnum chargeType;

  @Column(name = "cost_amount", nullable = false, precision = 16, scale = 8)
  private BigDecimal costAmount;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "currency", columnDefinition = "public.currency_enum")
  private CurrencyEnum currency;

  @Column(name = "usage_start_time", nullable = false)
  private OffsetDateTime usageStartTime;

  @Column(name = "usage_end_time", nullable = false)
  private OffsetDateTime usageEndTime;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata;
}