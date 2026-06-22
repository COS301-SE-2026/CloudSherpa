package com.cloudsherpa.ingestion.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.controller.CloudUsageController;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CloudUsageController.class)
@Import(CloudUsageService.class)
@AutoConfigureMockMvc(addFilters = false)
class CloudUsageControllerIntegrationTest {

  @SpringBootApplication(scanBasePackages = "com.cloudsherpa.ingestion.controller")
  static class TestApp {}

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CloudConnectorFactory connectorFactory;

  @MockitoBean private SherpaDbPersistenceService sherpaDbPersistenceService;

  @MockitoBean private AwsCloudConnector awsConnector;

  @BeforeEach
  void setUp() {

    when(connectorFactory.getConnector("AWS")).thenReturn(awsConnector);

    doNothing().when(sherpaDbPersistenceService).recordMetric(any(), any(), any());
  }

  @Test
  void ingestionEndpointShouldIntegrateControllerAndService() throws Exception {

    UsageRecordModel usage = new UsageRecordModel();

    usage.setProvider("AWS");
    usage.setMetricName("CPUUtilization");
    usage.setValue(55.0);
    usage.setResourceId("i-test123");

    when(awsConnector.fetchUsage(any(), any())).thenReturn(List.of(usage));

    String requestJson =
        """
                {
                  "includeUsage": true,
                  "includeBilling": false,
                  "period": 300,
                  "from": "2026-05-20T00:00:00Z",
                  "to": "2026-05-20T01:00:00Z",
                  "scopes": [
                    {
                      "provider": "AWS",
                      "accountId": "123456789",
                      "serviceScopes": [
                        {
                          "name": "AWS/EC2",
                          "metrics": [
                            "CPUUtilization"
                          ],
                          "instances": [
                            {
                              "identifierName": "InstanceId",
                              "values": [
                                "i-test123"
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;

    mockMvc
        .perform(
            post("/api/events/ingest").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usage").isArray())
        .andExpect(jsonPath("$.usage[0].provider").value("AWS"))
        .andExpect(jsonPath("$.usage[0].metricName").value("CPUUtilization"))
        .andExpect(jsonPath("$.usage[0].value").value(55.0))
        .andExpect(jsonPath("$.usage[0].resourceId").value("i-test123"));
  }
}
