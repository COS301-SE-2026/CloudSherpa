package com.cloudsherpa.service.unit.persistconnection.aws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cloudsherpa.service.persistconnection.aws.controller.AwsConnectionPersistenceController;
import com.cloudsherpa.service.persistconnection.aws.dto.AwsCredentialsDto;
import com.cloudsherpa.service.persistconnection.aws.dto.PersistAwsConnectionRequest;
import com.cloudsherpa.service.persistconnection.aws.dto.ResourceSelectionDto;
import com.cloudsherpa.service.persistconnection.aws.service.AwsConnectionPersistenceService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AwsConnectionPersistenceControllerTest {

  @Mock private AwsConnectionPersistenceService persistenceService;

  @Mock private Jwt jwt;

  @InjectMocks private AwsConnectionPersistenceController controller;

  @Captor private ArgumentCaptor<PersistAwsConnectionRequest> requestCaptor;

  private PersistAwsConnectionRequest request;
  private UUID userId;

  @BeforeEach
  void setUp() {

    userId = UUID.randomUUID();

    AwsCredentialsDto credentials = new AwsCredentialsDto("accessKey", "secretKey");

    ResourceSelectionDto resource =
        new ResourceSelectionDto(
            "i-12345",
            "EC2",
            "instanceId",
            "instance-1",
            "af-south-1",
            Map.of("Environment", "Prod"),
            true);

    request =
        new PersistAwsConnectionRequest(null, "Production", 300, credentials, List.of(resource));
  }

  @Test
  void shouldPersistConnection() {

    when(jwt.getSubject()).thenReturn(userId.toString());

    ResponseEntity<Void> response = controller.persistConnection(jwt, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    verify(persistenceService).persistConnection(requestCaptor.capture());

    PersistAwsConnectionRequest captured = requestCaptor.getValue();

    assertEquals(userId, captured.userId());
    assertEquals(request.displayName(), captured.displayName());
    assertEquals(request.credentials(), captured.credentials());
    assertEquals(request.resources(), captured.resources());
    assertEquals(request.ingestionPeriod(), captured.ingestionPeriod());
  }

  @Test
  void shouldReturnUnauthorizedWhenJwtIsNull() {

    ResponseEntity<Void> response = controller.persistConnection(null, request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

    verifyNoInteractions(persistenceService);
  }

  @Test
  void shouldReplaceExistingUserIdWithJwtSubject() {

    UUID originalUserId = UUID.randomUUID();

    request = request.withUserId(originalUserId);

    when(jwt.getSubject()).thenReturn(userId.toString());

    controller.persistConnection(jwt, request);

    verify(persistenceService).persistConnection(requestCaptor.capture());

    PersistAwsConnectionRequest captured = requestCaptor.getValue();

    assertEquals(userId, captured.userId());
  }

  @Test
  void shouldPropagateServiceException() {

    when(jwt.getSubject()).thenReturn(userId.toString());

    doThrow(new RuntimeException("Persistence failed"))
        .when(persistenceService)
        .persistConnection(any());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> controller.persistConnection(jwt, request));

    assertEquals("Persistence failed", exception.getMessage());
  }

  @Test
  void shouldReturnBadRequestWhenJwtSubjectIsInvalidUuid() {

    when(jwt.getSubject()).thenReturn("not-a-uuid");

    ResponseEntity<Void> response = controller.persistConnection(jwt, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    verifyNoInteractions(persistenceService);
  }

  @Test
  void shouldCallServiceExactlyOnce() {

    when(jwt.getSubject()).thenReturn(userId.toString());

    controller.persistConnection(jwt, request);

    verify(persistenceService, times(1)).persistConnection(any());

    verifyNoMoreInteractions(persistenceService);
  }
}
