package com.cloudsherpa.service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
// This code will execute exactly one time per incoming HTTP request, no matter what happens
// internally.
public class TenantInitializer extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      // If a user is logged in, Spring Security holds their UUID in auth.getName()
      // ("anonymousUser" is what Spring calls people who haven't logged in yet)
      if (auth != null && !auth.getName().equals("anonymousUser")) {
        TenantContext.setCurrentTenant(auth.getName());
      }

      chain.doFilter(req, res);

    } finally {
      TenantContext.clear();
    }
  }
}
