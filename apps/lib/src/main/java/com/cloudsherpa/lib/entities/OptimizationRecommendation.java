package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "optimization_recommendation")
public class OptimizationRecommendation {

  @Id
  private UUID recommendationId;

  @Column(nullable = false)
  private UUID resourceId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ProviderEnum provider;

  @Column(nullable = false)
  private String ruleId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private OptimizationActionTypeEnum actionType;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private OptimizationStatusEnum status;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> evidence;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  public OptimizationRecommendation() {}

  public OptimizationRecommendation(
      UUID resourceId,
      ProviderEnum provider,
      String ruleId,
      OptimizationActionTypeEnum actionType,
      OptimizationStatusEnum status,
      Map<String, Object> evidence,
      OffsetDateTime createdAt) {
    this.recommendationId = UUID.randomUUID();
    this.resourceId = resourceId;
    this.provider = provider;
    this.ruleId = ruleId;
    this.actionType = actionType;
    this.status = status;
    this.evidence = evidence;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getRecommendationId() {
    return recommendationId;
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public ProviderEnum getProvider() {
    return provider;
  }

  public String getRuleId() {
    return ruleId;
  }

  public OptimizationActionTypeEnum getActionType() {
    return actionType;
  }

  public OptimizationStatusEnum getStatus() {
    return status;
  }

  public void setStatus(OptimizationStatusEnum status) {
    this.status = status;
  }

  public Map<String, Object> getEvidence() {
    return evidence;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}