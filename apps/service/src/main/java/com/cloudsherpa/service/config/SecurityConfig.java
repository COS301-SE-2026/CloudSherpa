package com.cloudsherpa.service.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final String[] allowedOrigins;
  private final String mode;

  SecurityConfig(
      @Value("${allowed_origins:http://localhost:3000}") String[] allowedOrigins,
      @Value("${mode:prod}") String mode) {
    this.allowedOrigins = allowedOrigins;
    this.mode = mode;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Different servlet filters based on dev or prod
    if (mode.equals("dev")) {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    } else {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
          // If we do httponly to reduce XSS risk, we reintroduce CSRF risk, so mitigate by
          // including token
          .csrf(Customizer.withDefaults())
          // unguarded endpoints for the moment
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    }
  }

  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(allowedOrigins));
    // Add methods as we need them, explicitly keep to methods we are actually using
    config.setAllowedMethods(List.of("GET", "POST"));
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // All endpoints /**
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
