package com.cloudsherpa.service.listener;

import com.cloudsherpa.service.sse.SseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Listens for Postgres NOTIFY events and forwards them to SSE clients.
// Spring creates this component at startup and manages its lifecycle.
@Component
public class PostgresNotificationListener implements SmartLifecycle {
  // These inject database credentials from application.properties
  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.username}")
  private String dbUser;

  @Value("${spring.datasource.password}")
  private String dbPassword;

  // The standard JDBC connection used to talk to Postgres.
  private Connection connection;

  // The Postgres-specific connection that supports LISTEN/NOTIFY.
  private PGConnection pgConnection;

  // Jackson tool to parse raw JSON strings into Java objects
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final SseService sseService;

  private final ActiveListeners activeListeners;

  private volatile boolean running;

  private static final Logger logger = LoggerFactory.getLogger(PostgresNotificationListener.class);

  PostgresNotificationListener(SseService sseService, ActiveListeners activeListeners) {
    this.sseService = sseService;
    this.activeListeners = activeListeners;
  }

  // Creates a long-lived connection and registers the LISTEN channel.
  // This is called from start() and also used to reconnect if needed.
  private void initialize() {
    if (!running) {
      return;
    }

    try {
      if (connection != null && !connection.isClosed()) {
        return;
      }

      // We use DriverManager to create a BRAND NEW, raw connection.
      // Listeners hold connections open forever
      connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

      // We "unwrap" the generic wrapper to expose the underlying, Postgres-specific
      // PGConnection object, which gives us access to Postgres-only methods.
      // A standard java.sql.Connection doesn't have a getNotifications() method.
      // We "unwrap" it to reveal the underlying Postgres driver class, which does
      // have that method.
      pgConnection = connection.unwrap(PGConnection.class);

      // Register this connection to listen to the specific channel
      // We send a raw SQL command to the database.
      // This registers this specific connection ID with Postgres.
      // Postgres will now hold any messages sent to metric_events in a buffer
      // specifically for this
      // connection.

      loadAndListenTenantMetricEvents();
    } catch (SQLException e) {
      logger.warn("Failed to initialize Postgres notification listener", e);
    }
  }

  // Spring Boot runs this method on a background thread every 2 seconds.
  @Scheduled(fixedRate = 2000)
  public void pollForNotifications() {
    if (!running) {
      return;
    }

    // We check for any connection errors or database failures.
    try {
      if (!checkConnection()) {
        return;
      }
    } catch (SQLException e) {
      return;
    }

    try {
      // We ask the Postgres driver for messages.
      // The '0' means timeout is zero.
      // It tells the driver: "Give me what you have right now, but do not wait if
      // it's empty."
      // This prevents our Spring scheduling thread from freezing up.
      PGNotification[] notifications = pgConnection.getNotifications(0);

      if (notifications != null && notifications.length > 0) {
        for (PGNotification notification : notifications) {
          // This retrieves the actual text payload we sent from the database trigger
          // Thanks to row_to_json(NEW), it should be a JSON string representing a
          // database row.
          String eventName = notification.getName();
          logger.info("NOTIFIED {}", eventName);

          UUID userId = activeListeners.getUserIdForChannel(eventName);
          if (userId == null) {
            activeListeners.refreshTenantMetricEvents();
            loadAndListenTenantMetricEvents();
            userId = activeListeners.getUserIdForChannel(eventName);
          }

          if (userId == null) {
            logger.warn("No tenant mapping found for Postgres notification channel {}", eventName);
            continue;
          }

          String payload = notification.getParameter();
          processMetricForAnalytics(payload, userId);
        }
      }
    } catch (SQLException e) {
      logger.warn("Failed to poll Postgres notifications", e);
    }
  }

  // Parse and forward the metric to any connected SSE clients.
  private void processMetricForAnalytics(String payload, UUID userId) {
    try {
      // Parse the raw string back into a JSON object
      JsonNode event = objectMapper.readTree(payload);

      // This is where we would call intelligence engine to do its thing
      sseService.broadcast(userId, "metric", event);
    } catch (Exception e) {
      logger.warn("Failed to parse metric payload: {}", payload, e);
    }
  }

  private void loadAndListenTenantMetricEvents() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      for (String channel : activeListeners.getTenantMetricChannels()) {
        stmt.addBatch(
            "LISTEN " // NOSONAR Dynamically constructing query is the only way to subscribe
                + channel);
        // to tenant metric event channels
        logger.info("Listening for Postgres notifications on {}", channel);
      }
      stmt.executeBatch();
    }
  }

  private boolean checkConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      // Try to rebuild the connection
      initialize();

      // If it's still null, the DB is unreachable. We exit the method gracefully.
      // Spring will try running this method again in 2 seconds.
      if (pgConnection == null) {
        return false;
      }
    }

    return true;
  }

  // SmartLifecycle: called by Spring when the context starts.
  @Override
  public void start() {
    running = true;
    initialize();
  }

  @Override
  public boolean isAutoStartup() {
    return false;
  }

  // SmartLifecycle: called by Spring when the context stops.
  @Override
  public void stop() {
    stop(() -> {});
  }

  // SmartLifecycle: stop with a callback so Spring can wait for cleanup.
  @Override
  public void stop(Runnable callback) {
    running = false;
    closeConnection();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  // Close the DB connection and clear references so we can reconnect later.
  private void closeConnection() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
      connection = null;
      pgConnection = null;
    } catch (SQLException e) {
      logger.warn("Failed to close Postgres notification listener connection", e);
    }
  }
}
