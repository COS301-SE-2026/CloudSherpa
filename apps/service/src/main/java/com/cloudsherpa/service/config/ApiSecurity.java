package com.cloudsherpa.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class ApiSecurity {

  private final String mode;

  ApiSecurity(@Value("${mode:prod}") String mode) {
    this.mode = mode;
  }

  @Bean
  @Order(2)
  SecurityFilterChain apiSecurityFilterChain(
      HttpSecurity http,
      BearerTokenResolver bearerTokenResolver,
      UrlBasedCorsConfigurationSource corsConfigurationSource)
      throws Exception {
    if (mode.equals("dev")) {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
          .securityMatcher("/**")
          .csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    } else {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
          .securityMatcher("/**")
          .csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .oauth2ResourceServer(
              oauth2 ->
                  oauth2.bearerTokenResolver(bearerTokenResolver).jwt(Customizer.withDefaults()))
          .build();
    }
  }
}
