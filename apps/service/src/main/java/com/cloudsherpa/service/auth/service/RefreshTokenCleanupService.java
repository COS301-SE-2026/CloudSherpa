package com.cloudsherpa.service.auth.service;

import com.cloudsherpa.lib.repositories.RefreshTokenRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenCleanupService {

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Scheduled(cron = "@daily", zone = "UTC")
  @Transactional
  public void deleteExpiredAndRevokedTokens() {
    refreshTokenRepository.deleteByRevokedAtIsNotNullOrExpiresAtBefore(
        OffsetDateTime.now(ZoneOffset.UTC));
  }
}
