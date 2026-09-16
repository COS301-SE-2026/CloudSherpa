package com.cloudsherpa.service.webhooks.controller;

import com.cloudsherpa.service.webhooks.events.WebhookEvent;
import com.cloudsherpa.service.webhooks.model.Webhook;
import com.cloudsherpa.service.webhooks.model.WebhookDelivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
@Tag(name = "Webhooks", description = "CloudSherpa Webhooks CRUD operations")
public class WebhooksController {

  @Operation(summary = "Get all webhooks for the current user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Succesfully returned all webhooks",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = Webhook.class)),
                    examples =
                        @ExampleObject(
                            name = "Webhook list",
                            value =
                                """
                          [
                            {
                              "id": "c2a49b36-55d2-4233-96dd-cc4e3ae97671",
                              "name": "General alerts",
                              "endpointUrl": "https://example.com/webhooks/general",
                              "eventTypes": ["usage.threshold", "resource.discovery"],
                              "status": "ACTIVE",
                              "cloudAccounts": [
                                "c2a49b36-55d2-4233-96dd-cc4e3ae97672"
                              ]
                            },
                            {
                              "id": "c2a49b36-55d2-4233-96dd-cc4e3ae97673",
                              "name": "Billing update",
                              "endpointUrl": "https://example.com/webhooks/billing-update",
                              "eventTypes": ["billing.ingestion"],
                              "status": "PAUSED",
                              "cloudAccounts": [
                                "c2a49b36-55d2-4233-96dd-cc4e3ae97672"
                              ]
                            }
                          ]
                          """)))
      })
  @GetMapping()
  public List<Webhook> getWebhooks() {
    return List.of();
  }

  @Operation(summary = "Get all webhook deliveries for the current user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Succesfully returned all webhook deliveries",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = WebhookDelivery.class)),
                    examples =
                        @ExampleObject(
                            name = "Webhook delivery list",
                            value =
                                """
                          [
                            {
                              "deliveryId": "7d5905cf-3518-42c0-8f23-7a71a7cc5d58",
                              "timestamp": "2026-09-16T08:32:15Z",
                              "webhookId": "c2a49b36-55d2-4233-96dd-cc4e3ae97671",
                              "eventType": "usage.threshold",
                              "cloudAccount": "c2a49b36-55d2-4233-96dd-cc4e3ae97672",
                              "result": "DELIVERED",
                              "responseCode": 200
                            },
                            {
                              "deliveryId": "8c307a33-6d4a-4c35-bf14-c643d6b1f3c6",
                              "timestamp": "2026-09-16T08:45:02Z",
                              "webhookId": "c2a49b36-55d2-4233-96dd-cc4e3ae97673",
                              "eventType": "billing.ingestion",
                              "cloudAccount": "c2a49b36-55d2-4233-96dd-cc4e3ae97672",
                              "result": "FAILED",
                              "responseCode": 503
                            }
                          ]
                          """)))
      })
  @GetMapping("deliveries")
  public List<WebhookDelivery> getDeliveries() {
    return List.of();
  }

  @Operation(summary = "Get all supported webhook events")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Succesfully returned all supported webhook events",
            content =
                @Content(
                    schema =
                        @Schema(
                            type = "object",
                            description = "Webhook events grouped by cloud account display name",
                            additionalPropertiesSchema = WebhookEvent.class),
                    examples =
                        @ExampleObject(
                            name = "Webhook events by cloud account",
                            value =
                                """
                          {
                            "Usage Threshold Alerts": {
                              "id": "msg_demo_usage_threshold",
                              "type": "usage.threshold",
                              "timestamp": "2026-09-16T08:32:15Z",
                              "account": {
                                "name": "Azure production",
                                "provider": "AZURE"
                              },
                              "data": {
                                "metric": "cost",
                                "threshold": 500.00,
                                "currentValue": 612.45
                              }
                            },
                            "Billing Ingestion": {
                              "id": "msg_demo_billing_ingestion",
                              "type": "billing.ingestion",
                              "timestamp": "2026-09-16T08:45:02Z",
                              "account": {
                                "name": "AWS staging",
                                "provider": "AWS"
                              },
                              "data": {
                                "past14Days": 200 ,
                                "forecasted14Days": 220
                              }
                            }
                          }
                          """)))
      })
  @GetMapping("events")
  public Map<String, WebhookEvent<?>> getEvents() {
    return Map.of();
  }

  public void addWebhook() {}

  public void editWebhook() {}

  public void deleteWebhook() {}
}
