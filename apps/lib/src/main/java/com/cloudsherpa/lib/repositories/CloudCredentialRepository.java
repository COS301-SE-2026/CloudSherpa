package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudCredential;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudCredentialRepository extends JpaRepository<CloudCredential, UUID> {
  // Find all credentials for a specific account
  List<CloudCredential> findByAccountId(UUID accountId);

  // Find all credentials for a specific provider
  List<CloudCredential> findByProvider(String provider);

  // Find all credentials of a specific type
  List<CloudCredential> findByCredentialType(String credentialType);

  // Find all credentials for a specific account and provider
  List<CloudCredential> findByAccountIdAndProvider(UUID accountId, String provider);

  // Find by CloudAccount object
  List<CloudCredential> findByAccount(CloudAccount account);
}
