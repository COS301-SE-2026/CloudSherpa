package com.cloudsherpa.service.unit.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.UserRepository;
import com.cloudsherpa.service.auth.dto.AuthUserResponse;
import com.cloudsherpa.service.auth.dto.LoginRequest;
import com.cloudsherpa.service.auth.dto.RegisterRequest;
import com.cloudsherpa.service.auth.service.AuthService;
import com.cloudsherpa.service.auth.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;

  @Mock JwtService jwtService;

  private String validEmail;
  private String validPassword;
  private String validUsername;
  private AuthService authService;
  private RegisterRequest validRegisterRequest;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, jwtService);

    this.validEmail = "test@gmail.com";
    this.validUsername = "TestUser";
    this.validPassword = "SomeValidPassword@123";

    this.validRegisterRequest = new RegisterRequest(validEmail, validUsername, validPassword);
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

  @Test
  void registerShouldThrowWhenDuplicateUser() {
    // Arrange
    when(userRepository.existsByEmailIgnoreCase(validEmail)).thenReturn(true);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.register(validRegisterRequest));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
  }

  @Test
  void registerShouldNotThrowWhenValidRegistration() {
    assertDoesNotThrow(() -> this.authService.register(validRegisterRequest));
  }

  @Test
  void loginShouldThrowWhenEmailMissing() {
    // Arrange
    LoginRequest requestWithoutEmail = loginRequest(null, validPassword);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.login(requestWithoutEmail));
    assertBadRequest(exception);
  }

  @Test
  void loginShouldThrowWhenPasswordMissing() {
    // Arrange
    LoginRequest requestWithoutPassword = loginRequest(validEmail, null);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.authService.login(requestWithoutPassword));
    assertBadRequest(exception);
  }

  @Test
  void loginShouldThrowWhenInvalidCredentials() {
    // Arrange
    LoginRequest requestWithInvalidCredentials = loginRequest(validEmail, validPassword);
    when(userRepository.findByEmailIgnoreCase(validEmail)).thenReturn(null);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.authService.login(requestWithInvalidCredentials));
    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  @Test
  void loginShouldReturnUserWhenValidCredentials() {
    // Arrange
    UUID userId = UUID.randomUUID();
    String token = "valid.jwt.token";
    String passwordHash = new BCryptPasswordEncoder(12).encode(validPassword);
    User user = new User(userId, validEmail, validUsername, passwordHash);
    LoginRequest validLoginRequest = loginRequest(validEmail, validPassword);

    when(userRepository.findByEmailIgnoreCase(validEmail)).thenReturn(user);
    when(jwtService.generateToken(user)).thenReturn(token);

    // Act
    AuthUserResponse response = this.authService.login(validLoginRequest);

    // Assert
    assertEquals(userId, response.getUserId());
    assertEquals(validEmail, response.getEmail());
    assertEquals(validUsername, response.getUsername());
    assertEquals(token, response.getToken());
  }

  private void assertBadRequest(ResponseStatusException exception) {
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
  }

  private LoginRequest loginRequest(String email, String password) {
    LoginRequest request = new LoginRequest();
    // Use reflection to set private fields
    ReflectionTestUtils.setField(request, "email", email);
    ReflectionTestUtils.setField(request, "password", password);
    return request;
  }
}
