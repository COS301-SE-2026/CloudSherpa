package com.cloudsherpa.ingestion.unit.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudsherpa.ingestion.config.AuthenticationService;
import com.cloudsherpa.ingestion.config.SecurityAllowConfig;
import com.cloudsherpa.ingestion.config.SecurityApiConfig;
import com.cloudsherpa.ingestion.controller.CloudResourceController;
import com.cloudsherpa.ingestion.service.CloudResourceService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(CloudResourceController.class)
@ContextConfiguration(
    classes = {
      SecurityConfigTest.TestApp.class,
      SecurityConfigTest.TestActuatorController.class,
      CloudResourceController.class
    })
@Import({SecurityApiConfig.class, SecurityAllowConfig.class, AuthenticationService.class})
@TestPropertySource(properties = "sherpa.api-key=123")
@AutoConfigureMockMvc
class SecurityConfigTest {
  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApp {}

  @RestController
  static class TestActuatorController {
    @GetMapping("/actuator/health")
    Map<String, String> health() {
      return Map.of("status", "UP");
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CloudResourceService cloudResourceService;

  @Test
  void protectedEndpointShouldRejectUnauthorized() throws Exception {
    mockMvc
        .perform(post("/api/cloud-resources/resources/aws"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unprotectedEndpointShouldNotBeRejected() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void authenticatedRequestShouldNotBeRejected() throws Exception {
    mockGetAllOfferedServices();

    mockMvc
        .perform(
            post("/api/cloud-resources/services")
                .header("X-API-KEY", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"aws\""))
        .andExpect(status().isOk());
  }

  @Test
  void invalidApiKeyShouldBeRejected() throws Exception {
    mockGetAllOfferedServices();

    mockMvc
        .perform(
            post("/api/cloud-resources/services")
                .header("X-API-KEY", "12")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"aws\""))
        .andExpect(status().isUnauthorized());
  }

  private void mockGetAllOfferedServices() {
    when(cloudResourceService.getAllOfferedServices("aws")).thenReturn(List.of("EC2", "S3"));
  }
}
