package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.Budget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

  List<Budget> findByUserId(UUID userId);
  
  List<Budget> findByScopeAndScopeId(String scope, UUID scopeId);

  List<Budget> findByScopeAndScopeIdAndEnabledTrue(String scope, UUID scopeId);

  List<Budget> findByUserIdAndScopeAndEnabledTrue(UUID userId, String scope);
}