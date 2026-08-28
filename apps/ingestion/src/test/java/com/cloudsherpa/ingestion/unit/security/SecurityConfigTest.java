package com.cloudsherpa.ingestion.unit.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudsherpa.ingestion.service.CloudResourceService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "sherpa.api-key=123",
      "AES_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    })
@AutoConfigureMockMvc
class SecurityConfigTest {
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
