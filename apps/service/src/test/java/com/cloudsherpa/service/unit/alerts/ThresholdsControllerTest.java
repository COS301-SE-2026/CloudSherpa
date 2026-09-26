package com.cloudsherpa.service.unit.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Threshold;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.lib.repositories.ThresholdRepository;
import com.cloudsherpa.service.alerts.controller.ThresholdsController;
import com.cloudsherpa.service.alerts.dto.CreateThresholdRequest;
import com.cloudsherpa.service.alerts.dto.ThresholdResponse;
import com.cloudsherpa.service.alerts.dto.UpdateThresholdRequest;
import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ThresholdsControllerTest {

  @Mock private ThresholdRepository thresholdRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private MetricDisplayNameMapper metricMapper;

  private ThresholdsController controller;
  private UUID resourceId;

  @BeforeEach
  void setUp() {
    controller = new ThresholdsController(thresholdRepository, resourceRepository, metricMapper);
    resourceId = UUID.randomUUID();
  }

  @Test
  void createThresholdShouldMapCanonicalAndDisplayNames() {
    CreateThresholdRequest request = mock(CreateThresholdRequest.class);
    when(request.metricName()).thenReturn("CPU Utilization");
    when(request.operator()).thenReturn("GT");
    when(request.resourceId()).thenReturn(resourceId);
    when(request.value()).thenReturn(85.0);

    when(resourceRepository.findProviderByResourceId(resourceId)).thenReturn(ProviderEnum.AWS);

    when(metricMapper.toCanonicalName("AWS", "CPU Utilization")).thenReturn("CPUUtilization");

    Threshold savedThreshold = new Threshold();
    savedThreshold.setThresholdId(UUID.randomUUID());
    savedThreshold.setMetricName("CPUUtilization");

    when(thresholdRepository.save(any(Threshold.class))).thenReturn(savedThreshold);

    when(metricMapper.toDisplayName("CPUUtilization")).thenReturn("CPU Utilization");

    ResponseEntity<ThresholdResponse> response = controller.createThreshold(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ArgumentCaptor<Threshold> captor = ArgumentCaptor.forClass(Threshold.class);
    verify(thresholdRepository).save(captor.capture());

    assertEquals("CPUUtilization", captor.getValue().getMetricName());

    verify(metricMapper).toDisplayName("CPUUtilization");
  }

  @Test
  void listThresholdsShouldMapDisplayNamesForReturnedData() {
    Threshold threshold = new Threshold();
    threshold.setThresholdId(UUID.randomUUID());
    threshold.setMetricName("compute.googleapis.com/instance/cpu/utilization");

    when(thresholdRepository.findAll()).thenReturn(List.of(threshold));
    when(metricMapper.toDisplayName("compute.googleapis.com/instance/cpu/utilization"))
        .thenReturn("CPU Utilization");

    ResponseEntity<List<ThresholdResponse>> response = controller.listThresholds(null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(metricMapper).toDisplayName("compute.googleapis.com/instance/cpu/utilization");
  }

  @Test
  void updateThresholdShouldMapCanonicalNameWhenMetricNameIsUpdated() {
    UUID thresholdId = UUID.randomUUID();
    Threshold existingThreshold = new Threshold();
    existingThreshold.setResourceId(resourceId);
    existingThreshold.setMetricName("old.canonical.name");
    existingThreshold.setOperator("LT");

    UpdateThresholdRequest request = mock(UpdateThresholdRequest.class);
    when(request.metricName()).thenReturn("Network In");
    lenient().when(request.operator()).thenReturn("GT");

    when(thresholdRepository.findById(thresholdId)).thenReturn(Optional.of(existingThreshold));
    when(resourceRepository.findProviderByResourceId(resourceId)).thenReturn(ProviderEnum.AZURE);

    when(metricMapper.toCanonicalName("AZURE", "Network In")).thenReturn("Network In Total");

    ResponseEntity<Void> response = controller.updateThreshold(thresholdId, request);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    ArgumentCaptor<Threshold> captor = ArgumentCaptor.forClass(Threshold.class);
    verify(thresholdRepository).save(captor.capture());

    assertEquals("Network In Total", captor.getValue().getMetricName());
  }

  @Test
  void createThresholdShouldReturnBadRequestForInvalidOperator() {
    CreateThresholdRequest request = mock(CreateThresholdRequest.class);
    when(request.metricName()).thenReturn("CPUUtilization");
    when(request.operator()).thenReturn("INVALID_OP");

    ResponseEntity<ThresholdResponse> response = controller.createThreshold(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(thresholdRepository, never()).save(any());
  }

  @Test
  void updateThresholdShouldReturnBadRequestIfValueIsNotFinite() {
    UUID thresholdId = UUID.randomUUID();
    UpdateThresholdRequest request = mock(UpdateThresholdRequest.class);
    lenient().when(request.value()).thenReturn(Double.POSITIVE_INFINITY);

    ResponseEntity<Void> response = controller.updateThreshold(thresholdId, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(thresholdRepository, never()).save(any());
  }
}
