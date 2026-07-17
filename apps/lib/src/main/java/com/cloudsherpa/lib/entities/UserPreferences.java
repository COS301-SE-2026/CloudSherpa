package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "preferences", schema = "public")
public class UserPreferences {

  @Id
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;

  @Column(name = "theme", length = 20)
  private String theme;

  @Column(name = "background", columnDefinition = "text")
  private String background;

  @Column(name = "currency", length = 10)
  private String currency;

  @Column(name = "language", length = 10)
  private String language;

  @Column(name = "sidebar_toggle")
  private Boolean sidebarToggle;

  protected UserPreferences() {}

  public UserPreferences(
      UUID userId,
      String theme,
      String background,
      String currency,
      String language,
      Boolean sidebarToggle) {
    this.userId = userId;
    this.theme = theme;
    this.background = background;
    this.currency = currency;
    this.language = language;
    this.sidebarToggle = sidebarToggle;
  }

  public UUID getUserId() {
    return userId;
  }

  public User getUser() {
    return user;
  }

  public String getTheme() {
    return theme;
  }

  public String getBackground() {
    return background;
  }

  public String getCurrency() {
    return currency;
  }

  public String getLanguage() {
    return language;
  }

  public Boolean getSidebarToggle() {
    return sidebarToggle;
  }

  public void setTheme(String theme) {
    this.theme = theme;
  }
}