package com.cloudsherpa.service.optimization;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("optimization/recommendations")
@Tag(name = "Optimization Recommendations")
public class OptimizationRecommendationController {
  // private final OptimizationRecommendationService service

  public OptimizationRecommendationController() {
    // this.service = service
  }
}
