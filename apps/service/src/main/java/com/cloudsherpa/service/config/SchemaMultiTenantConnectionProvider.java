package com.cloudsherpa.service.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

@Component
public class SchemaMultiTenantConnectionProvider
    implements MultiTenantConnectionProvider<String> { // String representing the user_id

  // Pool of database connections
  private final DataSource dataSource;

  public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public Connection getConnection(String tenantId) throws SQLException {
    Connection connection = getAnyConnection();

    String schema = "";

    if (tenantId.equals("public")) {
      schema = "public";
    } else {
      schema = "tenant_" + tenantId.replace("-", "_");
    }

    try (var stmt = connection.createStatement()) {
      stmt.execute("SET search_path TO " + schema + ", public;");
      // When this connection is handed back to Hibernate, and Hibernate runs SELECT * FROM
      // resource,
      // PostgreSQL automatically routes it into that specific user's private tables.
    }

    return connection;
  }

  @Override
  public void releaseConnection(String tenantId, Connection connection) throws SQLException {

    try (var stmt = connection.createStatement()) {
      stmt.execute("SET search_path TO public;");
      // Reset the connection to the public connection for security
      // to ensure that users do not see eachother's data
    }
    connection.close();
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    return null;
  }
}
