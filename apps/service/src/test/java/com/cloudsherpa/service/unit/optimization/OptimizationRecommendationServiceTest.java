package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationRecommendation;
import com.cloudsherpa.lib.entities.OptimizationStatusEnum;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.OptimizationRecommendationRepository;
import com.cloudsherpa.service.optimization.service.OptimizationRecommendationService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptimizationRecommendationServiceTest {

  @Mock private OptimizationRecommendationRepository repository;

  @InjectMocks private OptimizationRecommendationService service;

  private OptimizationRecommendation activeRec;
  private UUID recId;

  @BeforeEach
  void setUp() {
    recId = UUID.randomUUID();
    activeRec = new OptimizationRecommendation();
    activeRec.setRecommendationId(recId);
    activeRec.setResourceId(UUID.randomUUID());
    activeRec.setProvider(ProviderEnum.AWS);
    activeRec.setActionType(OptimizationActionTypeEnum.DOWNSIZE);
    activeRec.setStatus(OptimizationStatusEnum.ACTIVE);
  }

  @Test
  void testGetRecommendationSummaryAggregatesCorrectly() {
    OptimizationRecommendation dismissedRec = new OptimizationRecommendation();
    dismissedRec.setActionType(OptimizationActionTypeEnum.TERMINATE);
    dismissedRec.setStatus(OptimizationStatusEnum.DISMISSED);

    when(repository.findAll()).thenReturn(List.of(activeRec, dismissedRec));

    Map<String, Object> summary = service.getRecommendationSummary();

    assertEquals(2, summary.get("total"));
    assertEquals(1, summary.get("active"));
    assertEquals(1, summary.get("dismissed"));

    Map<String, Integer> actionTypes = (Map<String, Integer>) summary.get("actionType");
    assertEquals(1, actionTypes.get("DOWNSIZE"));
    assertEquals(1, actionTypes.get("TERMINATE"));
  }

  @Test
  void testAcknowledgeRecommendation_UpdatesStatus() {
    when(repository.findById(recId)).thenReturn(Optional.of(activeRec));
    when(repository.save(any(OptimizationRecommendation.class))).thenReturn(activeRec);

    Map<String, Object> result = service.acknowledgeRecommendation(recId);

    assertEquals("ACKNOWLEDGED", result.get("status"));
    verify(repository).save(activeRec);
  }

  @Test
  void testReEnableRecommendation_FailsIfNotDismissed() {
    when(repository.findById(recId)).thenReturn(Optional.of(activeRec));

    Map<String, Object> result = service.reEnableRecommendation(recId);

    assertTrue(result.containsKey("error"));
    assertEquals("Only dismissed recommendations can be re-enabled", result.get("error"));
  }

  @Test
  void testReEnableRecommendationSucceedsIfDismissed() {
    activeRec.setStatus(OptimizationStatusEnum.DISMISSED);
    when(repository.findById(recId)).thenReturn(Optional.of(activeRec));

    Map<String, Object> result = service.reEnableRecommendation(recId);

    assertTrue(result.containsKey("message"));
    verify(repository).deleteById(recId);
  }
}
