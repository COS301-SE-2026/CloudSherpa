package com.cloudsherpa.service.unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.config.TenantInitializer;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TenantInitializerTest {

  private TenantInitializer tenantInitializer;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain filterChain;
  private SecurityContext securityContext;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    tenantInitializer = new TenantInitializer();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    filterChain = mock(FilterChain.class);
    securityContext = mock(SecurityContext.class);
    authentication = mock(Authentication.class);

    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void cleanUp() {
    TenantContext.clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternalSetsTenantIdWhenValidUserAuthenticated() throws Exception {
    String userId = "real-user-uuid-1234";
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(userId);

    ReflectionTestUtils.invokeMethod(
        tenantInitializer, "doFilterInternal", request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternalDoesNotSetTenantWhenUserIsAnonymous() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("anonymousUser");

    ReflectionTestUtils.invokeMethod(
        tenantInitializer, "doFilterInternal", request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternalDoesNotSetTenantWhenAuthenticationIsNull() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(null);

    ReflectionTestUtils.invokeMethod(
        tenantInitializer, "doFilterInternal", request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
