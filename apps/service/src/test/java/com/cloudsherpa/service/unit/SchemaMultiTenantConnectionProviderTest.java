package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.config.SchemaMultiTenantConnectionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaMultiTenantConnectionProviderTest {

  @Mock private DataSource dataSource;
  @Mock private Connection connection;
  @Mock private Statement statement;

  @InjectMocks private SchemaMultiTenantConnectionProvider provider;

  @Test
  void getAnyConnectionReturnsDataSourceConnection() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    Connection actual = provider.getAnyConnection();

    assertEquals(connection, actual);
  }

  @Test
  void releaseAnyConnectionClosesConnection() throws SQLException {
    provider.releaseAnyConnection(connection);
    verify(connection).close();
  }

  @Test
  void getConnectionSetsSchemaToPublicWhenTenantIsPublic() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);

    provider.getConnection("public");

    verify(statement).execute("SET search_path TO public, public;");
    verify(statement).close();
  }

  @Test
  void getConnectionSetsSchemaToTenantWhenTenantIsUuid() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    String tenantId = "abc-123";

    provider.getConnection(tenantId);

    verify(statement).execute("SET search_path TO tenant_abc_123, public;");
    verify(statement).close();
  }

  @Test
  void releaseConnectionResetsSchemaToPublicAndCloses() throws SQLException {
    when(connection.createStatement()).thenReturn(statement);

    provider.releaseConnection("any-tenant", connection);

    verify(statement).execute("SET search_path TO public;");
    verify(statement).close();
    verify(connection).close();
  }
}
