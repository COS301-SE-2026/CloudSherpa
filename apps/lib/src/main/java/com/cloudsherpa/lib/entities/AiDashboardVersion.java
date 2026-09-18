package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_dashboard_version", schema = "public")
public class AiDashboardVersion {

  @Id
  @Column(name = "version_id")
  private UUID versionId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "version_number", nullable = false)
  private Integer versionNumber;

  @Column(name = "parent_version_id")
  private UUID parentVersionId;

  @Column(name = "title", nullable = false, length = 80)
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "time_from")
  private OffsetDateTime timeFrom;

  @Column(name = "time_to")
  private OffsetDateTime timeTo;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "predefined_time", columnDefinition = "public.predefined_time_enum")
  private PredefinedTimeEnum predefinedTime;

  @Column(name = "current")
  private Boolean current;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected AiDashboardVersion() {
  }

  private AiDashboardVersion(Builder builder) {
    this.versionId = builder.versionId;
    this.sessionId = builder.sessionId;
    this.versionNumber = builder.versionNumber;
    this.parentVersionId = builder.parentVersionId;
    this.title = builder.title;
    this.description = builder.description;
    this.timeFrom = builder.timeFrom;
    this.timeTo = builder.timeTo;
    this.predefinedTime = builder.predefinedTime;
    this.current = builder.current;
    this.createdAt = builder.createdAt;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public Integer getVersionNumber() {
    return versionNumber;
  }

  public UUID getParentVersionId() {
    return parentVersionId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public OffsetDateTime getTimeFrom() {
    return timeFrom;
  }

  public OffsetDateTime getTimeTo() {
    return timeTo;
  }

  public PredefinedTimeEnum getPredefinedTime() {
    return predefinedTime;
  }

  public Boolean getCurrent() {
    return current;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID versionId;
    private UUID sessionId;
    private Integer versionNumber;
    private UUID parentVersionId;
    private String title;
    private String description;
    private OffsetDateTime timeFrom;
    private OffsetDateTime timeTo;
    private PredefinedTimeEnum predefinedTime;
    private Boolean current;
    private OffsetDateTime createdAt;

    public Builder versionId(UUID versionId) {
      this.versionId = versionId;
      return this;
    }

    public Builder sessionId(UUID sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder versionNumber(Integer versionNumber) {
      this.versionNumber = versionNumber;
      return this;
    }

    public Builder parentVersionId(UUID parentVersionId) {
      this.parentVersionId = parentVersionId;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder timeFrom(OffsetDateTime timeFrom) {
      this.timeFrom = timeFrom;
      return this;
    }

    public Builder timeTo(OffsetDateTime timeTo) {
      this.timeTo = timeTo;
      return this;
    }

    public Builder predefinedTime(PredefinedTimeEnum predefinedTime) {
      this.predefinedTime = predefinedTime;
      return this;
    }

    public Builder current(Boolean current) {
      this.current = current;
      return this;
    }

    public Builder createdAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public AiDashboardVersion build() {
      return new AiDashboardVersion(this);
    }
  }
}
