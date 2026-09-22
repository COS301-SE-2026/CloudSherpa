package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "budgets")
public class Budget {

  @Id
  @Column(name = "budget_id", nullable = false, updatable = false)
  private UUID budgetId;

  @Column(name = "user_id")
  private UUID userId;

  @jakarta.persistence.ManyToOne
  @jakarta.persistence.JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @Column(name = "scope", nullable = false, length = 20)
  private String scope;

  @Column(name = "scope_id")
  private UUID scopeId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "currency", columnDefinition = "public.currency_enum")
  private CurrencyEnum currency;

  @Column(name = "window_days", nullable = false)
  private Integer windowDays;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  public Budget() {}

  public Budget(
      UUID userId,
      String scope,
      UUID scopeId,
      BigDecimal amount,
      CurrencyEnum currency,
      Integer windowDays,
      boolean enabled) {
    this.budgetId = UUID.randomUUID();
    this.userId = userId;
    this.scope = scope;
    this.scopeId = scopeId;
    this.amount = amount;
    this.currency = currency;
    this.windowDays = windowDays;
    this.enabled = enabled;
  }

  public UUID getBudgetId() {
    return budgetId;
  }

  public void setBudgetId(UUID budgetId) {
    this.budgetId = budgetId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public UUID getScopeId() {
    return scopeId;
  }

  public void setScopeId(UUID scopeId) {
    this.scopeId = scopeId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public CurrencyEnum getCurrency() {
    return currency;
  }

  public void setCurrency(CurrencyEnum currency) {
    this.currency = currency;
  }

  public Integer getWindowDays() {
    return windowDays;
  }

  public void setWindowDays(Integer windowDays) {
    this.windowDays = windowDays;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}