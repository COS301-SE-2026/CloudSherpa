package com.cloudsherpa.ingestion.unit.provider.gcp.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.monitoring.GcpCloudMonitoringMetricProvider;
import com.cloudsherpa.ingestion.service.IngestionPersistenceService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.Aggregation;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class GcpCloudMonitoringMetricProviderTest {

  private GcpCloudMonitoringMetricProvider provider;

  private MetricServiceClient client;
  private GoogleCredentials googleCredentials;

  private AccountScope accountScope;
  private ServiceScope serviceScope;
  private InstanceScope instanceScope;
  private Instance instance;
  private Metric metric;
  private CloudCredentials credentials;
  private IngestionRequestEvent request;
  private IngestionPersistenceService persistenceService;
  private Normalizer normalizer;

  private MockedStatic<GcpClientFactory> gcpClientFactory;
  private MockedStatic<MetricServiceClient> metricServiceClientStatic;

  private final Instant from = Instant.parse("2026-09-02T12:00:00Z");

  private final Instant to = Instant.parse("2026-09-02T12:05:00Z");

  @BeforeEach
  void setUp() {
    persistenceService = mock(IngestionPersistenceService.class);
    provider = new GcpCloudMonitoringMetricProvider(persistenceService);

    client = mock(MetricServiceClient.class);
    googleCredentials = mock(GoogleCredentials.class);

    accountScope = mock(AccountScope.class);
    serviceScope = mock(ServiceScope.class);
    instanceScope = mock(InstanceScope.class);
    instance = mock(Instance.class);
    metric = mock(Metric.class);
    credentials = mock(CloudCredentials.class);
    request = mock(IngestionRequestEvent.class);
    normalizer = mock(Normalizer.class);

    // Request construction
    when(request.getCredentials()).thenReturn(credentials);
    when(request.getFrom()).thenReturn(from);
    when(request.getTo()).thenReturn(to);
    when(request.getPeriod()).thenReturn(60);

    // GCP credentials
    when(credentials.getProjectId()).thenReturn("test-project");

    // Account configuration
    when(accountScope.getAccountId()).thenReturn("account-123");
    when(accountScope.getProjectId()).thenReturn("test-project");

    // ServiceScope configuration
    when(accountScope.getServiceScopes()).thenReturn(List.of(serviceScope));

    when(serviceScope.getName()).thenReturn("gce_instance");

    when(serviceScope.getInstances()).thenReturn(List.of(instanceScope));

    when(serviceScope.getMetrics()).thenReturn(List.of(metric));

    // InstanceScope configuration
    when(instanceScope.getIdentifierName()).thenReturn("instance_id");

    when(instanceScope.getInstances()).thenReturn(List.of(instance));

    // Instance configuration
    when(instance.getIdentifier()).thenReturn("instance-123");

    when(instance.getRegion()).thenReturn("us-central1-a");

    // Metric configuration
    when(metric.getName()).thenReturn("compute.googleapis.com/instance/cpu/utilization");

    when(metric.getUnit()).thenReturn("1");

    gcpClientFactory = mockStatic(GcpClientFactory.class);

    gcpClientFactory
        .when(() -> GcpClientFactory.credentials(credentials))
        .thenReturn(googleCredentials);

    metricServiceClientStatic = mockStatic(MetricServiceClient.class);

    metricServiceClientStatic
        .when(() -> MetricServiceClient.create(any(MetricServiceSettings.class)))
        .thenReturn(client);
  }

  @AfterEach
  void tearDown() {
    metricServiceClientStatic.close();
    gcpClientFactory.close();
  }

  @Test
  void collectMetrics_whenMultipleInstancesAndMetrics_shouldCreateCorrectNumberOfRequests() {

    Instance instanceOne = mock(Instance.class);
    Instance instanceTwo = mock(Instance.class);

    when(instanceOne.getIdentifier()).thenReturn("instance-1");

    when(instanceOne.getRegion()).thenReturn("us-central1-a");

    when(instanceTwo.getIdentifier()).thenReturn("instance-2");

    when(instanceTwo.getRegion()).thenReturn("us-central1-b");

    Metric metricOne = mock(Metric.class);
    Metric metricTwo = mock(Metric.class);
    Metric metricThree = mock(Metric.class);

    when(metricOne.getName()).thenReturn("metric.one");

    when(metricOne.getUnit()).thenReturn("unit");

    when(metricTwo.getName()).thenReturn("metric.two");

    when(metricTwo.getUnit()).thenReturn("unit");

    when(metricThree.getName()).thenReturn("metric.three");

    when(metricThree.getUnit()).thenReturn("unit");

    when(instanceScope.getInstances()).thenReturn(List.of(instanceOne, instanceTwo));

    when(serviceScope.getMetrics()).thenReturn(List.of(metricOne, metricTwo, metricThree));

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    // 2 instances * 3 metrics = 6 GCP requests
    verify(client, times(6)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_shouldBuildCorrectProjectNameAndFilter() {

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    ListTimeSeriesRequest actual = captor.getValue();

    assertEquals("projects/test-project", actual.getName());

    assertEquals(
        "resource.type=\"gce_instance\" "
            + "AND resource.labels.\"instance_id\"=\"instance-123\" "
            + "AND metric.type=\"compute.googleapis.com/instance/cpu/utilization\"",
        actual.getFilter());
  }

  @Test
  void collectMetrics_shouldUseFullTimeSeriesView() {

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals(ListTimeSeriesRequest.TimeSeriesView.FULL, captor.getValue().getView());
  }

  @Test
  void collectMetrics_shouldBuildCorrectTimeInterval() {

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    TimeInterval interval = captor.getValue().getInterval();

    assertEquals(from, Instant.ofEpochSecond(interval.getStartTime().getSeconds()));

    assertEquals(to, Instant.ofEpochSecond(interval.getEndTime().getSeconds()));
  }

  @Test
  void collectMetrics_shouldBuildCorrectAggregation() {

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    Aggregation aggregation = captor.getValue().getAggregation();

    assertEquals(60, aggregation.getAlignmentPeriod().getSeconds());

    assertEquals(Aggregation.Aligner.ALIGN_MEAN, aggregation.getPerSeriesAligner());
  }

  @Test
  void collectMetrics_shouldUseConfiguredPeriod() {

    when(request.getPeriod()).thenReturn(300);

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals(300, captor.getValue().getAggregation().getAlignmentPeriod().getSeconds());
  }

  @Test
  void collectMetrics_shouldUseConfiguredProjectId() {

    when(credentials.getProjectId()).thenReturn("different-project");

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals("projects/different-project", captor.getValue().getName());
  }

  @Test
  void collectMetrics_shouldCloseClient() {

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    verify(client).close();
  }

  @Test
  void
      collectMetrics_whenCredentialsFactoryThrowsIOException_shouldThrowIllegalArgumentException() {

    gcpClientFactory
        .when(() -> GcpClientFactory.credentials(credentials))
        .thenThrow(new IOException("invalid credentials"));

    assertThrows(
        IllegalArgumentException.class,
        () -> provider.collectMetrics(accountScope, request, normalizer));

    verifyNoInteractions(client);
  }

  @Test
  void
      collectMetrics_whenMetricServiceClientCreationThrowsIOException_shouldThrowIllegalArgumentException() {

    metricServiceClientStatic
        .when(() -> MetricServiceClient.create(any(MetricServiceSettings.class)))
        .thenThrow(new IOException("invalid credentials"));

    assertThrows(
        IllegalArgumentException.class,
        () -> provider.collectMetrics(accountScope, request, normalizer));
  }

  @Test
  void collectMetrics_shouldCallGcpCredentialsFactoryWithRequestCredentials() {

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    gcpClientFactory.verify(() -> GcpClientFactory.credentials(credentials));
  }

  @Test
  void collectMetrics_shouldCreateOneRequestPerMetric() {

    Metric metricOne = mock(Metric.class);
    Metric metricTwo = mock(Metric.class);

    when(metricOne.getName()).thenReturn("metric.one");

    when(metricOne.getUnit()).thenReturn("unit");

    when(metricTwo.getName()).thenReturn("metric.two");

    when(metricTwo.getUnit()).thenReturn("unit");

    when(serviceScope.getMetrics()).thenReturn(List.of(metricOne, metricTwo));

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    verify(client, times(2)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_shouldCreateOneRequestPerInstance() {

    Instance instanceOne = mock(Instance.class);
    Instance instanceTwo = mock(Instance.class);

    when(instanceOne.getIdentifier()).thenReturn("instance-one");

    when(instanceOne.getRegion()).thenReturn("us-central1-a");

    when(instanceTwo.getIdentifier()).thenReturn("instance-two");

    when(instanceTwo.getRegion()).thenReturn("us-central1-b");

    when(instanceScope.getInstances()).thenReturn(List.of(instanceOne, instanceTwo));

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    verify(client, times(2)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_shouldBuildFilterUsingInstanceIdentifierName() {

    when(instanceScope.getIdentifierName()).thenReturn("resource_id");

    when(instance.getIdentifier()).thenReturn("resource-456");

    when(serviceScope.getName()).thenReturn("custom_resource");

    when(metric.getName()).thenReturn("custom.metric");

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals(
        "resource.type=\"custom_resource\" "
            + "AND resource.labels.\"resource_id\"=\"resource-456\" "
            + "AND metric.type=\"custom.metric\"",
        captor.getValue().getFilter());
  }

  @Test
  void collectMetrics_whenMultipleServices_shouldProcessAllServices() {

    ServiceScope secondServiceScope = mock(ServiceScope.class);

    InstanceScope secondInstanceScope = mock(InstanceScope.class);

    Instance secondInstance = mock(Instance.class);

    Metric secondMetric = mock(Metric.class);

    when(secondServiceScope.getName()).thenReturn("another_resource");

    when(secondServiceScope.getInstances()).thenReturn(List.of(secondInstanceScope));

    when(secondServiceScope.getMetrics()).thenReturn(List.of(secondMetric));

    when(secondInstanceScope.getIdentifierName()).thenReturn("resource_id");

    when(secondInstanceScope.getInstances()).thenReturn(List.of(secondInstance));

    when(secondInstance.getIdentifier()).thenReturn("resource-456");

    when(secondInstance.getRegion()).thenReturn("europe-west1-b");

    when(secondMetric.getName()).thenReturn("another.metric");

    when(secondMetric.getUnit()).thenReturn("1");

    when(accountScope.getServiceScopes()).thenReturn(List.of(serviceScope, secondServiceScope));

    configureClient();

    provider.collectMetrics(accountScope, request, normalizer);

    // One metric in the 1st service + one metric in the 2nd service
    verify(client, times(2)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  private void configureClient(TimeSeries... series) {
    MetricServiceClient.ListTimeSeriesPagedResponse response =
        mock(MetricServiceClient.ListTimeSeriesPagedResponse.class);

    when(response.iterateAll()).thenReturn(List.of(series));

    when(client.listTimeSeries(any(ListTimeSeriesRequest.class))).thenReturn(response);
  }
}
