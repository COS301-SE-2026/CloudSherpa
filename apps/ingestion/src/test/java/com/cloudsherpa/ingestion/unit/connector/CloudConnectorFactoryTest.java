package com.cloudsherpa.ingestion.unit.connector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudConnectorFactoryTest {

  private CloudConnectorFactory factory;
  private CloudConnector awsConnector;

  @BeforeEach
  void setUp() {
    awsConnector = mock(CloudConnector.class);

    factory = new CloudConnectorFactory(Map.of("AWS", awsConnector));
  }

  @Test
  void shouldReturnConnectorForUppercaseProvider() {
    CloudConnector result = factory.getConnector("AWS");

    assertNotNull(result);
    assertEquals(awsConnector, result);
  }

  @Test
  void shouldReturnConnectorForLowercaseProvider() {
    CloudConnector result = factory.getConnector("aws");

    assertNotNull(result);
    assertEquals(awsConnector, result);
  }

  @Test
  void shouldReturnConnectorForMixedCaseProvider() {
    CloudConnector result = factory.getConnector("AwS");

    assertEquals(awsConnector, result);
  }

  @Test
  void shouldThrowExceptionForUnknownProvider() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> factory.getConnector("NoProviderLikeThis"));

    assertTrue(ex.getMessage().contains("No connector found"));
  }

  @Test
  void shouldThrowExceptionForNullProvider() {
    assertThrows(NullPointerException.class, () -> factory.getConnector(null));
  }
}
