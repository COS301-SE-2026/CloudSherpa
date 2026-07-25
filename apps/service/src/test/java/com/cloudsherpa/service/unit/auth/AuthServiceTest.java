package com.cloudsherpa.service.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloudsherpa.lib.repositories.UserRepository;
import com.cloudsherpa.service.auth.dto.RegisterRequest;
import com.cloudsherpa.service.auth.service.AuthService;
import com.cloudsherpa.service.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;

  @Mock JwtService jwtService;

  private String validEmail;
  private String validPassword;
  private String validUsername;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, jwtService);

    this.validEmail = "test@gmail.com";
    this.validUsername = "TestUser";
    this.validPassword = "SomeValidPassword@123";
  }

  @Test
  void registerShouldThrowWhenEmailMissing() {
    // Arrange
    RegisterRequest requestWithoutEmail = new RegisterRequest(null, validUsername, validPassword);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.register(requestWithoutEmail));
    assertBadRequest(exception);
  }

  @Test
  void registerShouldThrowWhenInvalidEmail() {
    // Arrange
    RegisterRequest requestWithInvalidEmail =
        new RegisterRequest("testwithoutat", validUsername, validPassword);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.authService.register(requestWithInvalidEmail));
    assertBadRequest(exception);
  }

  @Test
  void registerShouldThrowWhenNoUsername() {
    // Arrange
    RegisterRequest requestWithoutUser = new RegisterRequest(validEmail, null, validPassword);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.register(requestWithoutUser));
    assertBadRequest(exception);
  }

  @Test
  void registerShouldThrowWhenNoPassword() {
    // Arrange
    RegisterRequest requestWithNoPassword = new RegisterRequest(validEmail, validUsername, null);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.register(requestWithNoPassword));
    assertBadRequest(exception);
  }

  @Test
  void registerShouldThrowWithWeakPassword() {
    // Arrange
    RegisterRequest requestWithNoPassword = new RegisterRequest(validEmail, validUsername, "weak");

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.register(requestWithNoPassword));
    assertBadRequest(exception);
  }

  private void assertBadRequest(ResponseStatusException exception) {
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
  }
}
