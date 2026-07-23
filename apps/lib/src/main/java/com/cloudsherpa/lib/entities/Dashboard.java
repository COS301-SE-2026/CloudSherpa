package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "dashboard", schema = "public")
public class Dashboard {

  @Id
  @Column(name = "dashboard_id", nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;

  @Column(name = "time_from")
  private OffsetDateTime timeFrom;

  @Column(name = "time_to")
  private OffsetDateTime timeTo;

  @Column(name = "predefined_time", length = 50)
  private String predefinedTime;

  @Column(name = "current", nullable = false)
  private Boolean current;

  protected Dashboard() {}

  public Dashboard(
      UUID id,
      UUID userId,
      String displayName,
      OffsetDateTime timeFrom,
      OffsetDateTime timeTo,
      String predefinedTime,
      Boolean current) {
    this.id = id;
    this.userId = userId;
    this.displayName = displayName;
    this.timeFrom = timeFrom;
    this.timeTo = timeTo;
    this.predefinedTime = predefinedTime;
    this.current = current;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public User getUser() {
    return user;
  }

  public OffsetDateTime getTimeFrom() {
    return timeFrom;
  }

  public OffsetDateTime getTimeTo() {
    return timeTo;
  }

  public String getPredefinedTime() {
    return predefinedTime;
  }

  public Boolean getCurrent() {
    return current;
  }
}