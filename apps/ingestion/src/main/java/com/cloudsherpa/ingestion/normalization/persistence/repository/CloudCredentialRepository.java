package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudConnection;
import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudCredential;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudCredentialRepository extends JpaRepository<CloudCredential, UUID> {
  // Find all credentials for a specific connection
  List<CloudCredential> findByConnectionId(UUID connectionId);

  // Find all credentials for a specific provider
  List<CloudCredential> findByProvider(String provider);

  // Find all credentials of a specific type
  List<CloudCredential> findByCredentialType(String credentialType);

  // Find all credentials for a specific connection and provider
  List<CloudCredential> findByConnectionIdAndProvider(UUID connectionId, String provider);

  // Find by CloudConnection object
  List<CloudCredential> findByConnection(CloudConnection connection);
}
