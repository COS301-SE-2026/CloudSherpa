package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.optimization.controller.OptimizationRecommendationController;
import com.cloudsherpa.service.optimization.service.OptimizationRecommendationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptimizationRecommendationControllerTest {

  @Mock private OptimizationRecommendationService service;

  @InjectMocks private OptimizationRecommendationController controller;

  private UUID recId;

  @BeforeEach
  void setUp() {
    recId = UUID.randomUUID();
  }

  @Test
  void testGetRecommendationsCallsService() {
    when(service.getRecommendations()).thenReturn(List.of(Map.of("status", "ACTIVE")));

    List<Map<String, Object>> result =
        controller.getRecommendations("ACTIVE", "AWS", null, null, null);

    assertEquals(1, result.size());
    verify(service).getRecommendations();
  }

  @Test
  void testApplyRecommendationCallsService() {
    when(service.applyRecommendation(recId)).thenReturn(Map.of("status", "APPLIED"));

    Map<String, Object> result = controller.applyRecommendation(recId);

    assertEquals("APPLIED", result.get("status"));
    verify(service).applyRecommendation(recId);
  }
}
