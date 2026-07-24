package com.cloudsherpa.service.preferences.service;

import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.cloudsherpa.lib.entities.LanguageEnum;
import com.cloudsherpa.lib.entities.ThemeEnum;
import com.cloudsherpa.lib.entities.UserPreferences;
import com.cloudsherpa.lib.repositories.PreferencesRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferencesService {

  private final PreferencesRepository preferencesRepository;

  public PreferencesService(PreferencesRepository preferencesRepository) {
    this.preferencesRepository = preferencesRepository;
  }

  public String getUserTheme(UUID userId) {
    return preferencesRepository
        .findById(userId)
        .map(UserPreferences::getTheme)
        .map(ThemeEnum::name)
        .orElse("dark");
  }

  @Transactional
  public void updateTheme(UUID userId, String newTheme) {
    ThemeEnum theme = ThemeEnum.valueOf(newTheme.toLowerCase());

    UserPreferences prefs =
        preferencesRepository
            .findById(userId)
            .orElseGet(
                () ->
                    new UserPreferences(
                        userId, theme, null, CurrencyEnum.USD, LanguageEnum.en, true));

    prefs.setTheme(theme);
    preferencesRepository.save(prefs);
  }
}
