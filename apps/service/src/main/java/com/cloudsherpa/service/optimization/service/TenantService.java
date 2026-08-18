package com.cloudsherpa.service.optimization.service;

import com.cloudsherpa.lib.repositories.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

  private final UserRepository userRepository;

  public TenantService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<UUID> findTenantIds() {
    return userRepository.findAllTenantIds();
  }
}
