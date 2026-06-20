package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.UserPreferences;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {
}