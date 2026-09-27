package com.cloudsherpa.service.unit.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.webhooks.consumers.WebhookEventProcessor;
import com.cloudsherpa.service.webhooks.consumers.WebhookEventRoutingService;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookEventRoutingServiceTest {
  @Mock PendingWebhookEventRepository pendingEventRepository;
  @Mock WebhookEventProcessor processor;
  @InjectMocks WebhookEventRoutingService routingService;

  private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID DELIVERY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void returnsEmptyListWhenTenantNotFound() {
    when(pendingEventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

    List<DeliveryTask> actual = routingService.createDeliveries(EVENT_ID);

    assertEquals(List.of(), actual);
    verify(processor, never()).createDeliveries(EVENT_ID);
  }

  @Test
  void clearsTenantContext() {
    mockFindTenantId();
    when(processor.createDeliveries(EVENT_ID)).thenReturn(List.of());
    TenantContext.setCurrentTenant("previous-tenant");

    routingService.createDeliveries(EVENT_ID);

    assertNull(TenantContext.getCurrentTenant());
  }

  @Test
  void setsTenantContext() {
    mockFindTenantId();
    when(processor.createDeliveries(EVENT_ID))
        .thenAnswer(
            invocation -> {
              assertEquals(TENANT_ID.toString(), TenantContext.getCurrentTenant());
              return List.of();
            });

    routingService.createDeliveries(EVENT_ID);

    verify(processor).createDeliveries(EVENT_ID);
  }

  @Test
  void createsDeliveries() {
    mockFindTenantId();
    List<DeliveryTask> expected = List.of(new DeliveryTask(TENANT_ID, DELIVERY_ID));
    when(processor.createDeliveries(EVENT_ID)).thenReturn(expected);

    List<DeliveryTask> actual = routingService.createDeliveries(EVENT_ID);

    assertEquals(expected, actual);
  }

  private void mockFindTenantId() {
    PendingWebhookEvent event = mock(PendingWebhookEvent.class);
    when(event.getTenantId()).thenReturn(TENANT_ID);
    when(pendingEventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
  }
}
