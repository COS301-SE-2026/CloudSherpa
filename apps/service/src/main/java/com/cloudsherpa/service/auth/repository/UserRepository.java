package com.cloudsherpa.service.auth.repository;

import com.cloudsherpa.service.auth.model.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  // Finds a user by email
  // Translates to: SELECT * FROM users WHERE LOWER(email) = LOWER(?)
  User findByEmailIgnoreCase(String email);

  // Checks if a user already has that email
  // Translates to: SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(?))
  boolean existsByEmailIgnoreCase(String email);
}
