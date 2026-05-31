package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  // can add specialized queries here

  // will probably only be for the user_id as that is the only field
  // relevant for the ingestion service
}
