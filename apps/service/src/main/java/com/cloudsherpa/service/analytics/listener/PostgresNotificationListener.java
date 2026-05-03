package com.cloudsherpa.service.analytics.listener;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Its job is to maintain a direct line to Postgres and quietly ask if any new events have arrived.
// Tells Spring Boot to create an instance of this class when the app starts.
@Component
public class PostgresNotificationListener 
{
    // These inject database credentials from application.properties
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // The standard JDBC connection
    private Connection connection;

    // The Postgres-specific connection that understands notifications
    private PGConnection pgConnection;

    // Jackson tool to parse raw JSON strings into Java objects
    private final ObjectMapper objectMapper = new ObjectMapper();

    // @PostConstruct forces this method to run exactly once, immediately after Spring creates this class.
    @PostConstruct
    public void initialize() 
    {
        try 
        {
            // We use DriverManager to create a BRAND NEW, raw connection.
            // Listeners hold connections open forever
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // We "unwrap" the generic wrapper to expose the underlying, Postgres-specific 
            // PGConnection object, which gives us access to Postgres-only methods.
            // A standard java.sql.Connection doesn't have a getNotifications() method.
            // We "unwrap" it to reveal the underlying Postgres driver class, which does have that method.
            pgConnection = connection.unwrap(PGConnection.class);

            // Register this connection to listen to the specific channel
            // We send a raw SQL command to the database. 
            // This registers this specific connection ID with Postgres. 
            // Postgres will now hold any messages sent to metric_events in a buffer specifically for this connection.
            try (Statement stmt = connection.createStatement()) 
            {
                stmt.execute("LISTEN metric_events");
            }
        } 
        catch (SQLException e) 
        {
            System.err.println(e.getMessage());
        }
    }

    // Spring Boot runs this method on a background thread every 2 seconds.
    @Scheduled(fixedRate = 2000)
    public void pollForNotifications() 
    {
        // We check for any connection errors or database failures.
        try 
        {
            if (connection == null || connection.isClosed()) 
            {
                // Try to rebuild the connection
                initialize();

                // If it's still null, the DB is unreachable. We exit the method gracefully.
                // Spring will try running this method again in 2 seconds.
                if (pgConnection == null) 
                {
                    return;
                }
            }
        } 
        catch (SQLException e) 
        {
            return;
        }

        try 
        {
            // We ask the Postgres driver for messages.
            // The '0' means timeout is zero. 
            // It tells the driver: "Give me what you have right now, but do not wait if it's empty."
            // This prevents our Spring scheduling thread from freezing up.
            PGNotification[] notifications = pgConnection.getNotifications(0);
            
            if (notifications != null && notifications.length > 0) 
            {
                for (PGNotification notification : notifications) 
                {
                    // This retrieves the actual text payload we sent from the database trigger
                    // Thanks to row_to_json(NEW), it should be a JSON string representing a database row.
                    String payload = notification.getParameter();
                    try 
                    {
                        // Parse the raw string back into a JSON object
                        JsonNode event = objectMapper.readTree(payload);
                        
                        // Pass the parsed JSON to the business logic
                        processMetricForAnalytics(event);
                    } 
                    catch (Exception e) 
                    {
                        System.err.println(e.getMessage());
                    }
                }
            }
        } 
        catch (SQLException e) 
        {
            System.err.println(e.getMessage());
        }
    }

    private void processMetricForAnalytics(JsonNode metric) 
    {
        // Now that the data is a JSON object, extract values defensively.
        double usageAmount = metric.path("usage_amount").asDouble(0.0);
        double costAmount = metric.path("cost_amount").asDouble(0.0);

        System.out.println("PROCESSING METRIC FOR ANALYTICS: usage=" + usageAmount + ", cost=" + costAmount);
        
        // This is where we would call intelligence engine to do its thing  
    }

    // @PreDestroy runs when the Spring Boot application is shutting down.
    @PreDestroy
    public void shutDown() 
    {
        try 
        {
            if (connection != null && !connection.isClosed()) 
            {
                connection.close();
            }
        } 
        catch (SQLException e) 
        {
            System.err.println(e.getMessage());
        }
    }
}