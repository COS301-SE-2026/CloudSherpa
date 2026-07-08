package com.cloudsherpa.service.config;

// * Every time a request arrives, this guard checks the user's JWT token.
// * If they are logged in, the guard writes their ID on the sticky note (TenantContext).
// * When the user is done and the response is sent, this guard shreds the sticky note
// * so the next person who connects doesn't accidentally see the previous person's data.

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantInitializer extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      if (auth instanceof JwtAuthenticationToken) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        Jwt jwt = jwtAuth.getToken();
        String tenantId = jwt.getClaimAsString("tenantId");
        if (tenantId != null) {
          TenantContext.setCurrentTenant(tenantId);
        }
      }

      chain.doFilter(req, res);

    } finally {
      TenantContext.clear();
    }
  }
}
