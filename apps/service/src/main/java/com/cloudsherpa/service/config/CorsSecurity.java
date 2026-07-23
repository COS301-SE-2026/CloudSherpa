package com.cloudsherpa.service.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class CorsSecurity {

  private final String[] allowedOrigins;

  CorsSecurity(@Value("${allowed_origins:http://localhost:3000}") String[] allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(allowedOrigins));
    config.setAllowCredentials(true);
    // Add methods as we need them, explicitly keep to methods we are actually using
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // All endpoints /**
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
