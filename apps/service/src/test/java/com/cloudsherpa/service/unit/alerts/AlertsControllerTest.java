package com.cloudsherpa.service.unit.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.service.alerts.controller.AlertsController;
import com.cloudsherpa.service.alerts.dto.AlertResponse;
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
class AlertsControllerTest {

  @Mock private AlertRepository alertRepository;

  private AlertsController controller;

  @BeforeEach
  void setUp() {
    controller = new AlertsController(alertRepository);
  }

  @Test
  void getAlertsShouldReturnAllAlertsWhenTypeIsNull() {
    Alert alert = new Alert();
    alert.setAlertId(UUID.randomUUID());

    when(alertRepository.findAll()).thenReturn(List.of(alert));

    ResponseEntity<List<AlertResponse>> response = controller.getAlerts(null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());

    verify(alertRepository).findAll();
  }

  @Test
  void getAlertsShouldFilterByAlertType() {
    Alert alert = new Alert();
    alert.setAlertId(UUID.randomUUID());

    when(alertRepository.findByAlertTypeOrderByCreatedAtDesc(AlertTypeEnum.ANOMALY))
        .thenReturn(List.of(alert));

    ResponseEntity<List<AlertResponse>> response = controller.getAlerts(AlertTypeEnum.ANOMALY);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    verify(alertRepository).findByAlertTypeOrderByCreatedAtDesc(AlertTypeEnum.ANOMALY);
  }

  @Test
  void getAlertShouldReturnNotFoundWhenAlertDoesNotExist() {
    UUID alertId = UUID.randomUUID();
    when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

    ResponseEntity<AlertResponse> response = controller.getAlert(alertId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void disableShouldUpdateStatusToDisabled() {
    UUID alertId = UUID.randomUUID();
    Alert alert = new Alert();
    alert.setStatus(AlertStatusEnum.ACTIVE);

    when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

    ResponseEntity<Void> response = controller.disable(alertId);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);

    verify(alertRepository).save(captor.capture());
    assertEquals(AlertStatusEnum.DISABLED, captor.getValue().getStatus());
  }

  @Test
  void enableShouldUpdateStatusToActive() {
    UUID alertId = UUID.randomUUID();
    Alert alert = new Alert();
    alert.setStatus(AlertStatusEnum.DISABLED);

    when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

    ResponseEntity<Void> response = controller.enable(alertId);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);

    verify(alertRepository).save(captor.capture());
    assertEquals(AlertStatusEnum.ACTIVE, captor.getValue().getStatus());
  }
}
