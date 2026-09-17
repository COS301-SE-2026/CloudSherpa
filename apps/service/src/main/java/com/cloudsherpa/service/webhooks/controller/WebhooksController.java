package com.cloudsherpa.service.webhooks.controller;

import com.cloudsherpa.service.webhooks.dto.AddWebhookDto;
import com.cloudsherpa.service.webhooks.dto.AddWebhookResponseDto;
import com.cloudsherpa.service.webhooks.dto.EditWebhookDto;
import com.cloudsherpa.service.webhooks.dto.WebhookEventDto;
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
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
                    examples =
                        @ExampleObject(
                            name = "Webhook events by category",
                            value =
                                """
                          {
                            "Dev": {
                              "Dev Event": {
                                "id": "msg_demo_dev_event",
                                "type": "dev.event",
                                "timestamp": "1970-01-01T00:00:00Z",
                                "account": {
                                  "name": "Development account",
                                  "provider": "AZURE"
                                },
                                "data": {
                                  "devString": "Dev Event String",
                                  "devDecimal": 20.0,
                                  "devTime": "1970-01-01T00:00:00Z"
                                }
                              }
                            },
                            "Usage": {
                              "Usage Threshold Alert": {
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
                              }
                            }
                          }
                          """)))
      })
  @GetMapping("events")
  public Map<String, Map<String, WebhookEventDto<?>>> getEvents() {
    return Map.of();
  }

  @Operation(summary = "Add a new webhook")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Succesfully added a new webhook",
            content =
                @Content(
                    schema = @Schema(implementation = AddWebhookResponseDto.class),
                    examples =
                        @ExampleObject(
                            name = "Example secret",
                            value =
                                """
                            {
                                "secret": "ceb3eebb7ad20566a86afc938ef49161d785791697acfa893210b4beadc3ffed"
                            }
                            """))),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
      })
  @PostMapping("add")
  public ResponseEntity<AddWebhookResponseDto> addWebhook(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Add webhook payload",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = AddWebhookDto.class),
                      examples =
                          @ExampleObject(
                              name = "Example payload",
                              value =
                                  """
                            {
                                "name": "Example Webhook",
                                "endpointUrl": "https://example.com/api",
                                "eventTypes": [
                                    "billing.ingestion",
                                    "usage.threshold"
                                ],
                                "cloudAccounts": [
                                    "c2a49b36-55d2-4233-96dd-cc4e3ae97672"
                                ]
                            }
                            """)))
          @RequestBody
          AddWebhookDto request) {
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Edit existing webhook")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Succesfully added a new webhook"),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  @PostMapping("edit/{webhookId}")
  public ResponseEntity<Void> editWebhook(
      @PathVariable("webhookId") UUID id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Edit webhook payload",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = EditWebhookDto.class),
                      examples =
                          @ExampleObject(
                              name = "Example payload",
                              value =
                                  """
                        {
                          "name": "Billing update",
                          "endpointUrl": "https://example.com/webhooks/billing-update",
                          "eventTypes": [
                            "billing.ingestion",
                            "usage.threshold"
                          ],
                          "status": "PAUSED",
                          "cloudAccounts": [
                            "c2a49b36-55d2-4233-96dd-cc4e3ae97672"
                          ]
                        }
                        """)))
          @RequestBody
          EditWebhookDto request) {
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete webhook")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description =
                "Deleted webhook / not, generic response code protects against information gathering via response codes")
      })
  @DeleteMapping("delete/webhookId")
  public ResponseEntity<Void> deleteWebhook() {
    return ResponseEntity.noContent().build();
  }
}
