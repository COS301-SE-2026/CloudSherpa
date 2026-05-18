package com.cloudsherpa.service.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final String[] allowedOrigins;
  private final String mode;
  private final String dashboardHost;

  SecurityConfig(
      @Value("${allowed_origins:http://localhost:3000}") String[] allowedOrigins,
      @Value("${mode:prod}") String mode,
      @Value("${dashboard-host:http://localhost:3000}") String dashboardHost) {
    this.allowedOrigins = allowedOrigins;
    this.mode = mode;
    this.dashboardHost = dashboardHost;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Different servlet filters based on dev or prod
    if (mode.equals("dev")) {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    } else {
      return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers("/auth/login", "/auth/register")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
          // redirect user to login
          .exceptionHandling(
              exception ->
                  exception.authenticationEntryPoint(
                      new LoginUrlAuthenticationEntryPoint(
                          String.format("%s/login", dashboardHost))))
          .build();
    }
  }

  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(allowedOrigins));
    config.setAllowCredentials(true);
    // Add methods as we need them, explicitly keep to methods we are actually using
    config.setAllowedMethods(List.of("GET", "POST"));
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // All endpoints /**
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
