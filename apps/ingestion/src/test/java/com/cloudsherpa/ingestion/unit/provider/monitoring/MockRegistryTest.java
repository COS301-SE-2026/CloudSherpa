package com.cloudsherpa.ingestion.unit.provider.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudsherpa.ingestion.provider.aws.monitoring.AwsMockRegistry;
import com.cloudsherpa.ingestion.provider.azure.monitoring.AzureMockRegistry;
import com.cloudsherpa.ingestion.provider.gcp.monitoring.GcpMockRegistry;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.factory.MetricDefinitionFactory;
import com.cloudsherpa.ingestion.provider.mock.registry.MockMetricRegistry;
import com.cloudsherpa.ingestion.provider.mock.simulation.BooleanEventSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.CounterSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.DurationSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.LatencySimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.LossSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.PercentageSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.StorageSimulator;
import com.cloudsherpa.ingestion.provider.mock.simulation.ThroughputSimulator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockRegistryTest {

  @Mock private PercentageSimulator percentageSimulator;

  @Mock private ThroughputSimulator throughputSimulator;

  @Mock private CounterSimulator counterSimulator;

  @Mock private LatencySimulator latencySimulator;

  @Mock private BooleanEventSimulator booleanEventSimulator;

  @Mock private LossSimulator lossSimulator;

  @Mock private DurationSimulator durationSimulator;

  @Mock private StorageSimulator storageSimulator;

  private GcpMockRegistry gcpRegistry;
  private AwsMockRegistry awsRegistry;
  private AzureMockRegistry azureRegistry;

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

    gcpRegistry = new GcpMockRegistry(metrics);
    awsRegistry = new AwsMockRegistry(metrics);
    azureRegistry = new AzureMockRegistry(metrics);
  }

  // Metric lookup

  @ParameterizedTest(name = "{0}: unknown metric should throw IllegalArgumentException")
  @EnumSource(RegistryType.class)
  void metric_whenMetricDoesNotExist_shouldThrowIllegalArgumentException(
      RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    MockServiceDefinition service =
        registry.services().stream()
            .findFirst()
            .orElseThrow(
                () -> new AssertionError("Registry contains no services: " + registryType));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.metric("unknown.metric"));

    assertEquals(
        "Unknown metric 'unknown.metric' for service " + service.serviceName(),
        exception.getMessage());
  }

  // Registry existence

  @ParameterizedTest(name = "{0}: registry should contain services")
  @EnumSource(RegistryType.class)
  void registry_shouldContainAtLeastOneService(RegistryType registryType) {
    MockMetricRegistry registry = registryFor(registryType);

    assertNotNull(registry);
    assertFalse(registry.services().isEmpty(), "Registry contains no services: " + registryType);
  }

  // Registry consistency

  @ParameterizedTest(name = "{0}: every service should have at least one metric")
  @EnumSource(RegistryType.class)
  void everyRegisteredService_shouldHaveAtLeastOneMetric(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(
            service ->
                assertTrue(
                    service.metricCount() > 0,
                    "Service has no metrics: " + service.serviceName() + " in " + registryType));
  }

  @ParameterizedTest(name = "{0}: every service should be retrievable by name")
  @EnumSource(RegistryType.class)
  void everyRegisteredService_shouldBeRetrievableByName(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(service -> assertSame(service, registry.service(service.serviceName())));
  }

  @ParameterizedTest(name = "{0}: service names should be unique")
  @EnumSource(RegistryType.class)
  void services_shouldHaveUniqueNames(RegistryType registryType) {
    MockMetricRegistry registry = registryFor(registryType);

    Set<String> serviceNames =
        registry.services().stream()
            .map(MockServiceDefinition::serviceName)
            .collect(Collectors.toSet());

    assertEquals(
        registry.services().size(),
        serviceNames.size(),
        "Duplicate service names found in " + registryType);
  }

  // Service lookup

  @ParameterizedTest(name = "{0}: unknown service should not exist")
  @EnumSource(RegistryType.class)
  void contains_whenServiceDoesNotExist_shouldReturnFalse(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    assertFalse(registry.contains("unknown_service"));
  }

  @ParameterizedTest(name = "{0}: null service should not exist")
  @EnumSource(RegistryType.class)
  void contains_whenServiceIsNull_shouldReturnFalse(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    assertFalse(registry.contains(null));
  }

  @ParameterizedTest(name = "{0}: unknown service lookup should throw IllegalArgumentException")
  @EnumSource(RegistryType.class)
  void service_whenServiceDoesNotExist_shouldThrowIllegalArgumentException(
      RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> registry.service("unknown_service"));

    assertEquals("No mock service registered for: unknown_service", exception.getMessage());
  }

  @ParameterizedTest(name = "{0}: null service lookup should throw IllegalArgumentException")
  @EnumSource(RegistryType.class)
  void service_whenServiceIsNull_shouldThrowIllegalArgumentException(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> registry.service(null));

    assertEquals("No mock service registered for: null", exception.getMessage());
  }

  // Metric consistency

  @ParameterizedTest(name = "{0}: every registered metric should be retrievable")
  @EnumSource(RegistryType.class)
  void everyRegisteredMetric_shouldBeRetrievable(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(
            service ->
                service
                    .metrics()
                    .forEach(
                        metric ->
                            assertSame(
                                metric,
                                service.metric(metric.name()),
                                "Metric could not be retrieved: "
                                    + metric.name()
                                    + " from service "
                                    + service.serviceName())));
  }

  @ParameterizedTest(name = "{0}: every metric should have a non-empty name")
  @EnumSource(RegistryType.class)
  void everyRegisteredMetric_shouldHaveName(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(
            service ->
                service
                    .metrics()
                    .forEach(
                        metric ->
                            assertFalse(
                                metric.name().isBlank(),
                                "Metric has blank name in service " + service.serviceName())));
  }

  @ParameterizedTest(name = "{0}: every metric should have a unit")
  @EnumSource(RegistryType.class)
  void everyRegisteredMetric_shouldHaveUnit(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(
            service ->
                service
                    .metrics()
                    .forEach(
                        metric ->
                            assertFalse(
                                metric.unit().isBlank(),
                                "Metric has blank unit: " + metric.name())));
  }

  @ParameterizedTest(name = "{0}: every metric should have a profile")
  @EnumSource(RegistryType.class)
  void everyRegisteredMetric_shouldHaveProfile(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(
            service ->
                service
                    .metrics()
                    .forEach(
                        metric ->
                            assertNotNull(
                                metric.profile(),
                                "Metric has no simulation profile: " + metric.name())));
  }

  @ParameterizedTest(name = "{0}: every metric should have a simulator")
  @EnumSource(RegistryType.class)
  void everyRegisteredMetric_shouldHaveSimulator(RegistryType registryType) {

    MockMetricRegistry registry = registryFor(registryType);

    registry
        .services()
        .forEach(
            service ->
                service
                    .metrics()
                    .forEach(
                        metric ->
                            assertNotNull(
                                metric.simulator(), "Metric has no simulator: " + metric.name())));
  }

  // Registry helper

  private MockMetricRegistry registryFor(RegistryType registryType) {
    return switch (registryType) {
      case GCP -> gcpRegistry;
      case AWS -> awsRegistry;
      case AZURE -> azureRegistry;
    };
  }

  private enum RegistryType {
    GCP,
    AWS,
    AZURE
  }
}
