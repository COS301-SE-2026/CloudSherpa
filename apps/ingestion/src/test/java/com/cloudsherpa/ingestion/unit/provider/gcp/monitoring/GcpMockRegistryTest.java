package com.cloudsherpa.ingestion.unit.provider.gcp.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudsherpa.ingestion.provider.gcp.monitoring.GcpMockRegistry;
import com.cloudsherpa.ingestion.provider.mock.definition.MetricDefinition;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.definition.Profiles;
import com.cloudsherpa.ingestion.provider.mock.factory.MetricDefinitionFactory;
import com.cloudsherpa.ingestion.provider.mock.simulation.BooleanEventSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.CounterSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.DurationSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.LatencySimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.LossSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.PercentageSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.StorageSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.ThroughputSimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GcpMockRegistryTest {

  @Mock private PercentageSimulator percentageSimulator;

  @Mock private ThroughputSimulator throughputSimulator;

  @Mock private CounterSimulator counterSimulator;

  @Mock private LatencySimulator latencySimulator;

  @Mock private BooleanEventSimulator booleanEventSimulator;

  @Mock private LossSimulator lossSimulator;

  @Mock private DurationSimulator durationSimulator;

  @Mock private StorageSimulator storageSimulator;

  private GcpMockRegistry registry;

  @BeforeEach
  void setUp() {
    MetricDefinitionFactory metrics =
        new MetricDefinitionFactory(
            percentageSimulator,
            throughputSimulator,
            counterSimulator,
            latencySimulator,
            booleanEventSimulator,
            lossSimulator,
            durationSimulator,
            storageSimulator);

    registry = new GcpMockRegistry(metrics);
  }

  // Metric lookup

  @Test
  void metric_shouldReturnCorrectMetricDefinition() {
    MockServiceDefinition service = registry.service("gce_instance");

    MetricDefinition metric = service.metric("compute.googleapis.com/instance/cpu/utilization");

    assertNotNull(metric);
    assertEquals("compute.googleapis.com/instance/cpu/utilization", metric.name());
    assertEquals("10^2.%", metric.unit());
    assertEquals(Profiles.CPU, metric.profile());
    assertSame(percentageSimulator, metric.simulator());
  }

  @Test
  void metric_whenMetricDoesNotExist_shouldThrowIllegalArgumentException() {
    MockServiceDefinition service = registry.service("gce_instance");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.metric("unknown.metric"));

    assertEquals(
        "Unknown metric 'unknown.metric' for service gce_instance", exception.getMessage());
  }

  // Registry consistency

  @Test
  void everyRegisteredService_shouldHaveAtLeastOneMetric() {
    registry
        .services()
        .forEach(
            service ->
                assertTrue(
                    service.metricCount() > 0, "Service has no metrics: " + service.serviceName()));
  }

  @Test
  void everyRegisteredService_shouldBeRetrievableByName() {
    registry
        .services()
        .forEach(service -> assertSame(service, registry.service(service.serviceName())));
  }
}
