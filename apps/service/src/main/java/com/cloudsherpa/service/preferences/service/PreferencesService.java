package com.cloudsherpa.service.preferences.service;

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
    return preferencesRepository.findById(userId).map(UserPreferences::getTheme).orElse("light");
  }

  @Transactional
  public void updateTheme(UUID userId, String newTheme) {
    UserPreferences prefs =
        preferencesRepository
            .findById(userId)
            .orElseGet(() -> new UserPreferences(userId, newTheme, null, "USD", "en", true));

    prefs.setTheme(newTheme.toLowerCase());
    preferencesRepository.save(prefs);
  }
}
