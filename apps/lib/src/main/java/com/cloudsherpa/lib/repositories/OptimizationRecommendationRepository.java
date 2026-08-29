package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.OptimizationRecommendation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizationRecommendationRepository
    extends JpaRepository<OptimizationRecommendation, UUID> {

  // Find active recommendations for a resource
  @Query(
      "SELECT o FROM OptimizationRecommendation o WHERE o.resourceId = :resourceId AND o.status = 'ACTIVE'")
  List<OptimizationRecommendation> findActiveByResourceId(@Param("resourceId") UUID resourceId);

  // Find existing recommendation by resource and rule
  @Query(
      "SELECT o FROM OptimizationRecommendation o WHERE o.resourceId = :resourceId AND o.ruleId = :ruleId")
  Optional<OptimizationRecommendation> findByResourceIdAndRuleId(
      @Param("resourceId") UUID resourceId, @Param("ruleId") String ruleId);

  // Find suppressed recommendations for a resource and rule (user dismissal)
  @Query(
      "SELECT o FROM OptimizationRecommendation o WHERE o.resourceId = :resourceId AND o.ruleId = :ruleId AND o.status = 'SUPPRESSED'")
  Optional<OptimizationRecommendation> findSuppressedByResourceIdAndRuleId(
      @Param("resourceId") UUID resourceId, @Param("ruleId") String ruleId);
}