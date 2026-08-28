package com.cloudsherpa.ingestion.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityAllowConfig {

  private final Environment environment;

  public SecurityAllowConfig(Environment environment) {
    this.environment = environment;
  }

  @Bean
  @Order(1)
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    List<String> unprotectedRoutes = new ArrayList<>();

    if (isProfileActive("dev")) {
      unprotectedRoutes.add("/**");
    } else {
      unprotectedRoutes.add("/actuator/health");
    }

    return http.securityMatcher(unprotectedRoutes.toArray(String[]::new))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  private boolean isProfileActive(String profile) {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch(profile::equals);
  }
}
