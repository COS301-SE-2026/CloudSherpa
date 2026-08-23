package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {
  // Finds a user by email
  // Translates to: SELECT * FROM users WHERE LOWER(email) = LOWER(?)
  User findByEmailIgnoreCase(String email);

  // Checks if a user already has that email
  // Translates to: SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(?))
  boolean existsByEmailIgnoreCase(String email);

  
  // Calls the PostgreSQL automation function to dynamically generate a new
  // schema (e.g., tenant_123e4567...) and all required metrics tables for a new user.
  @Transactional
  @Query(value = "SELECT public.create_new_tenant(:tenantId)", nativeQuery = true)
  void createTenantSchema(@Param("tenantId") UUID tenantId);

  @Query("select u.id from User u")
  List<UUID> findAllTenantIds();
}