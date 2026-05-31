package com.cloudsherpa.service.config;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

@Configuration
public class JwtConfig {
  @Bean
  public JwtDecoder jwtDecoder(@Value("${auth.jwt.secret}") String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    return NimbusJwtDecoder.withSecretKey(key).build();
  }

  @Bean
  public BearerTokenResolver bearerTokenResolver() {
    return request -> {
      if (request.getCookies() == null) {
        return null;
      }

      return Arrays.stream(request.getCookies())
          .filter(cookie -> "auth_token".equals(cookie.getName()))
          .map(Cookie::getValue)
          .findFirst()
          .orElse(null);
    };
  }
}
