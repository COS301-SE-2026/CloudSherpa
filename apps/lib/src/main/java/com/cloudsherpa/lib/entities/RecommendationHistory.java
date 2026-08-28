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
@Table(name = "recommendation_history")
public class RecommendationHistory {

  @Id
  private UUID historyId;

  @Column(nullable = false)
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
  private OptimizationStatusEnum previousStatus;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private OptimizationStatusEnum newStatus;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> evidence;

  @Column(nullable = false)
  private OffsetDateTime changedAt;

  public RecommendationHistory() {}

  public RecommendationHistory(
      UUID recommendationId,
      UUID resourceId,
      ProviderEnum provider,
      String ruleId,
      OptimizationActionTypeEnum actionType,
      OptimizationStatusEnum previousStatus,
      OptimizationStatusEnum newStatus,
      Map<String, Object> evidence,
      OffsetDateTime changedAt) {
    this.historyId = UUID.randomUUID();
    this.recommendationId = recommendationId;
    this.resourceId = resourceId;
    this.provider = provider;
    this.ruleId = ruleId;
    this.actionType = actionType;
    this.previousStatus = previousStatus;
    this.newStatus = newStatus;
    this.evidence = evidence;
    this.changedAt = changedAt;
  }

  public UUID getHistoryId() {
    return historyId;
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

  public OptimizationStatusEnum getPreviousStatus() {
    return previousStatus;
  }

  public OptimizationStatusEnum getNewStatus() {
    return newStatus;
  }

  public Map<String, Object> getEvidence() {
    return evidence;
  }

  public OffsetDateTime getChangedAt() {
    return changedAt;
  }
}