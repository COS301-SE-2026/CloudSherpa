package com.cloudsherpa.service.auth.service;

import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.UserRepository;
import com.cloudsherpa.service.auth.dto.AuthUserResponse;
import com.cloudsherpa.service.auth.dto.LoginRequest;
import com.cloudsherpa.service.auth.dto.RegisterRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;

  // uses a strong hashing algorithm and automatic salting
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

  public AuthService(UserRepository userRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
  }

  // REGISTER
  @Transactional
  public void register(RegisterRequest request) {
    String email = normalizeEmail(request.getEmail());
    String username = normalizeUsername(request.getUsername());
    String password = request.getPassword();

    if (email == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
    }
    if (!isEmailValid(email)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email format is invalid");
    }
    if (username == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
    }
    if (password == null || password.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
    }
    if (!isPasswordStrong(password)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Password must be at least 8 characters and include upper, lower, number, and symbol");
    }
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
    }

    String passwordHash = passwordEncoder.encode(password);

    UUID userId = UUID.randomUUID();
    User user = new User(userId, email, username, passwordHash);

    // write the newly created user to SherpaDB in the users table
    userRepository.save(user);

    // trigger the creation of the new user's personal schema
    userRepository.createTenantSchema(userId);
  }

  // LOGIN
  public AuthUserResponse login(LoginRequest request) {
    String email = normalizeEmail(request.getEmail());
    String password = request.getPassword();

    if (email == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
    }
    if (password == null || password.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
    }

    User user = userRepository.findByEmailIgnoreCase(email);
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    String token = jwtService.generateToken(user);
    return new AuthUserResponse(user.getId(), user.getEmail(), user.getUsername(), token);
  }

  private String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    String trimmed = email.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.toLowerCase();
  }

  private String normalizeUsername(String username) {
    if (username == null) {
      return null;
    }
    String trimmed = username.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed;
  }

  private boolean isEmailValid(String email) {
    return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  }

  private boolean isPasswordStrong(String password) {
    if (password == null) {
      return false;
    }
    return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s])(?!.*\\s).{8,}$");
  }
}
