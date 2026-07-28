package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "preferences", schema = "public")
public class UserPreferences {

  @Id
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "theme", columnDefinition = "public.theme_enum")
  private ThemeEnum theme;

  @Column(name = "background", columnDefinition = "text")
  private String background;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "currency", columnDefinition = "public.currency_enum")
  private CurrencyEnum currency;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "language", columnDefinition = "public.language_enum")
  private LanguageEnum language;

  @Column(name = "sidebar_toggle")
  private Boolean sidebarToggle;

  protected UserPreferences() {}

  public UserPreferences(
      UUID userId,
      ThemeEnum theme,
      String background,
      CurrencyEnum currency,
      LanguageEnum language,
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

  public ThemeEnum getTheme() {
    return theme;
  }

  public String getBackground() {
    return background;
  }

  public CurrencyEnum getCurrency() {
    return currency;
  }

  public LanguageEnum getLanguage() {
    return language;
  }

  public Boolean getSidebarToggle() {
    return sidebarToggle;
  }

  public void setTheme(ThemeEnum theme) {
    this.theme = theme;
  }
}
