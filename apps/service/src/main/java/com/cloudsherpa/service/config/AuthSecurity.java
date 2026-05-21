package com.cloudsherpa.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class AuthSecurity {

  private final String mode;

  AuthSecurity(@Value("${mode:prod}") String mode) {
    this.mode = mode;
  }

  @Bean
  @Order(1)
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, UrlBasedCorsConfigurationSource corsConfigurationSource) throws Exception {
    // Different servlet filters based on dev or prod
    if (mode.equals("dev")) {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
          .csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    } else {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
          .securityMatcher("/auth/**")
          .csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    }
  }
}
