package com.cloudsherpa.service.webhooks.controller;

import com.cloudsherpa.service.webhooks.model.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
                              "name": "General alerts",
                              "endpointUrl": "https://example.com/webhooks/general",
                              "eventTypes": ["usage.threshold", "resource.discovery"],
                              "status": "ACTIVE",
                              "cloudAccounts": [
                                "c2a49b36-55d2-4233-96dd-cc4e3ae97672"
                              ]
                            },
                            {
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

  public void getDeliveries() {}

  public void getEvents() {}

  public void addWebhook() {}

  public void editWebhook() {}

  public void deleteWebhook() {}
}
