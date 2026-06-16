package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudAccountRepository extends JpaRepository<CloudAccount, UUID> {
  // Find all accounts for a specific connection
  List<CloudAccount> findByConnectionId(UUID connectionId);

  // Find all accounts of a specific type
  List<CloudAccount> findByAccountType(String accountType);

  // Find all accounts for a specific connection and type
  List<CloudAccount> findByConnectionIdAndAccountType(UUID connectionId, String accountType);

  // Find by CloudConnection object
  List<CloudAccount> findByConnection(CloudConnection connection);
}
