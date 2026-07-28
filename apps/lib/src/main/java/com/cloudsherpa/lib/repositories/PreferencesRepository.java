package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PreferencesRepository extends JpaRepository<UserPreferences, UUID> {
}