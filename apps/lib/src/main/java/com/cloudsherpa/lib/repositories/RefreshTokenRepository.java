package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.RefreshToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(UUID userId);

  @Transactional
  long deleteByRevokedAtIsNotNullOrExpiresAtBefore(OffsetDateTime currentTime);
}