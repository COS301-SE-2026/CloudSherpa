package com.cloudsherpa.service.webhooks.controller;

import com.cloudsherpa.service.webhooks.WebhookEventDefinitionRegistry;
import com.cloudsherpa.service.webhooks.dto.AddWebhookDto;
import com.cloudsherpa.service.webhooks.dto.AddWebhookResponseDto;
import com.cloudsherpa.service.webhooks.dto.EditWebhookDto;
import com.cloudsherpa.service.webhooks.dto.WebhookDeliveryResponse;
import com.cloudsherpa.service.webhooks.dto.WebhookEventDto;
import com.cloudsherpa.service.webhooks.dto.WebhookResponse;
import com.cloudsherpa.service.webhooks.events.devevent.DevPayload;
import com.cloudsherpa.service.webhooks.exceptions.WebhookNotFoundException;
import com.cloudsherpa.service.webhooks.service.WebhookProducerService;
import com.cloudsherpa.service.webhooks.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
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

  private final WebhookEventDefinitionRegistry definitionRegistry;
  private final WebhookService webhookService;
  private final WebhookProducerService producerService;
  private final Environment environment;

  public WebhooksController(
      WebhookEventDefinitionRegistry definitionRegistry,
      WebhookService webhookService,
      WebhookProducerService producerService,
      Environment environment) {
    this.definitionRegistry = definitionRegistry;
    this.webhookService = webhookService;
    this.producerService = producerService;
    this.environment = environment;
  }

  @Operation(summary = "Get all webhooks for the current user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Succesfully returned all webhooks",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = WebhookResponse.class)),
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
  public List<WebhookResponse> getWebhooks() {
    return webhookService.getWebhooks();
  }

  @Operation(summary = "Get all webhook deliveries for the current user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Succesfully returned all webhook deliveries",
            content =
                @Content(
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = WebhookDeliveryResponse.class)),
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
  public List<WebhookDeliveryResponse> getDeliveries() {
    return webhookService.getWebhookDeliveries();
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
    return definitionRegistry.getEventDefinitions();
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
    AddWebhookResponseDto response = webhookService.addWebhook(request);
    return ResponseEntity.ok(response);
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

    try {
      webhookService.editWebhook(id, request);
      return ResponseEntity.ok().build();
    } catch (WebhookNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(summary = "Delete webhook")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description =
                "Deleted webhook / not, generic response code protects against information gathering via response codes")
      })
  @DeleteMapping("delete/{webhookId}")
  public ResponseEntity<Void> deleteWebhook(@PathVariable("webhookId") UUID webhookId) {
    webhookService.deleteWebhook(webhookId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("path")
  public ResponseEntity<Void> triggerDevEvent() {
    if (!environment.matchesProfiles("dev")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    DevPayload payload = new DevPayload("Test", BigDecimal.valueOf(2), Instant.now());

    producerService.produceEvent(
        UUID.fromString("5ebe4340-c5ec-4833-ad93-06abf4609f03"),
        UUID.fromString("a0000000-0000-0000-0000-000000000001"),
        "dev.event",
        payload);
    return ResponseEntity.ok().build();
  }
}
