package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record DashboardCreateDTO(UUID userId, UUID id, String displayName) {
  public DashboardCreateDTO withUserId(UUID userId) {
    return new DashboardCreateDTO(userId, id, displayName);
  }
}
