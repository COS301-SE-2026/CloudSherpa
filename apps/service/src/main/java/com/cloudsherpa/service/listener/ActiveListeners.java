package com.cloudsherpa.service.listener;

import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ActiveListeners {
  private static final Logger logger = LoggerFactory.getLogger(ActiveListeners.class);

  private final UserRepository userRepository;
  private final ObjectProvider<PostgresNotificationListener> postgresNotificationListener;
  private final Map<String, UUID> tenantMetricEvents = new ConcurrentHashMap<>();

  public ActiveListeners(
      UserRepository userRepository,
      ObjectProvider<PostgresNotificationListener> postgresNotificationListener) {
    this.userRepository = userRepository;
    this.postgresNotificationListener = postgresNotificationListener;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    refreshTenantMetricEvents();
    postgresNotificationListener.ifAvailable(PostgresNotificationListener::start);
  }

  public void refreshTenantMetricEvents() {
    for (User user : userRepository.findAll()) {
      tenantMetricEvents.putIfAbsent(toTenantMetricChannel(user.getId()), user.getId());
    }

    logger.info("Loaded {} tenant metric event listener mappings", tenantMetricEvents.size());
  }

  public UUID getUserIdForChannel(String channel) {
    return tenantMetricEvents.get(channel);
  }

  public List<String> getTenantMetricChannels() {
    return new ArrayList<>(tenantMetricEvents.keySet());
  }

  private String toTenantMetricChannel(UUID userId) {
    return "metric_events_tenant_" + userId.toString().replace("-", "_");
  }
}
