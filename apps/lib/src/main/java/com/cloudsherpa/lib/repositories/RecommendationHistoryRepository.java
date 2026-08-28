package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.RecommendationHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, UUID> {

}