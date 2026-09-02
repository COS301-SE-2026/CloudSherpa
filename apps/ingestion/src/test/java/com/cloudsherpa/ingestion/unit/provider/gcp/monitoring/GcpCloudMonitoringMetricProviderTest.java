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
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.monitoring.GcpCloudMonitoringMetricProvider;
import com.google.api.MonitoredResource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.Aggregation;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
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

  private MockedStatic<GcpClientFactory> gcpClientFactory;
  private MockedStatic<MetricServiceClient> metricServiceClientStatic;

  private final Instant from = Instant.parse("2026-09-02T12:00:00Z");

  private final Instant to = Instant.parse("2026-09-02T12:05:00Z");

  @BeforeEach
  void setUp() {
    provider = new GcpCloudMonitoringMetricProvider();

    client = mock(MetricServiceClient.class);
    googleCredentials = mock(GoogleCredentials.class);

    accountScope = mock(AccountScope.class);
    serviceScope = mock(ServiceScope.class);
    instanceScope = mock(InstanceScope.class);
    instance = mock(Instance.class);
    metric = mock(Metric.class);
    credentials = mock(CloudCredentials.class);
    request = mock(IngestionRequestEvent.class);

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
  void collectMetrics_whenDoubleValue_shouldReturnUsageRecord() throws Exception {

    Point point = createDoublePoint(42.5, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    TimeSeries series = createTimeSeries("instance-123", "us-central1-a", point);

    configureClient(series);

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());

    UsageRecordModel result = results.get(0);

    assertEquals("gce_instance", result.getServiceName());

    assertEquals("compute.googleapis.com/instance/cpu/utilization", result.getMetricName());

    assertEquals("instance_id", result.getResourceType());
    assertEquals("instance-123", result.getResourceId());
    assertEquals("1", result.getUnit());
    assertEquals("us-central1-a", result.getRegion());

    assertEquals(42.5, result.getValue());

    assertEquals(Instant.parse("2026-09-02T12:01:00Z"), result.getTimestamp());

    assertEquals(Instant.parse("2026-09-02T12:00:00Z"), result.getPeriodStart());

    assertEquals(Instant.parse("2026-09-02T12:01:00Z"), result.getPeriodEnd());

    assertEquals("account-123", result.getAccountId());
    assertEquals("account-123", result.getProjectId());

    assertEquals("GCP", result.getProvider());
    assertEquals("GCPMonitoringService", result.getSource());

    assertNotNull(result.getIngestionId());
    assertNotNull(result.getRecordId());

    assertNotNull(result.getDimensions());

    assertEquals("instance-123", result.getDimensions().get("instance_id"));

    assertEquals("us-central1-a", result.getDimensions().get("zone"));
  }

  @Test
  void collectMetrics_whenInt64Value_shouldConvertToDouble() throws Exception {

    Point point = createInt64Point(123L, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", point));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());
    assertEquals(123.0, results.get(0).getValue());
  }

  @Test
  void collectMetrics_whenBooleanTrue_shouldConvertToOne() throws Exception {

    Point point = createBooleanPoint(true, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", point));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());
    assertEquals(1.0, results.get(0).getValue());
  }

  @Test
  void collectMetrics_whenBooleanFalse_shouldConvertToZero() throws Exception {

    Point point = createBooleanPoint(false, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", point));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());
    assertEquals(0.0, results.get(0).getValue());
  }

  @Test
  void collectMetrics_whenValueIsUnset_shouldReturnZero() throws Exception {

    Point point =
        Point.newBuilder()
            .setInterval(createInterval("2026-09-02T12:00:00Z", "2026-09-02T12:01:00Z"))
            .build();

    configureClient(createTimeSeries("instance-123", "us-central1-a", point));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());
    assertEquals(0.0, results.get(0).getValue());
  }

  @Test
  void collectMetrics_whenMultiplePointsReturnedPerTimeseries_shouldCreateOneRecordPerPoint()
      throws Exception {

    Point first = createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    Point second = createDoublePoint(20.0, "2026-09-02T12:02:00Z", "2026-09-02T12:01:00Z");

    Point third = createDoublePoint(30.0, "2026-09-02T12:03:00Z", "2026-09-02T12:02:00Z");

    TimeSeries series = createTimeSeries("instance-123", "us-central1-a", first, second, third);

    configureClient(series);

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(3, results.size());

    assertEquals(10.0, results.get(0).getValue());
    assertEquals(20.0, results.get(1).getValue());
    assertEquals(30.0, results.get(2).getValue());
  }

  @Test
  void collectMetrics_whenMultipleSeriesReturned_shouldProcessAllSeries() throws Exception {

    TimeSeries first =
        createTimeSeries(
            "instance-123",
            "us-central1-a",
            createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z"));

    TimeSeries second =
        createTimeSeries(
            "instance-123",
            "us-central1-a",
            createDoublePoint(20.0, "2026-09-02T12:02:00Z", "2026-09-02T12:01:00Z"));

    configureClient(first, second);

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(2, results.size());

    assertEquals(10.0, results.get(0).getValue());
    assertEquals(20.0, results.get(1).getValue());
  }

  @Test
  void collectMetrics_whenNoTimeSeriesReturned_shouldReturnEmptyList() {

    configureClient();

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertNotNull(results);
    assertTrue(results.isEmpty());

    verify(client).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_whenNoServiceScopesInRequest_shouldReturnEmptyList() {

    when(accountScope.getServiceScopes()).thenReturn(Collections.emptyList());

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void collectMetrics_whenServiceScopeHasNoInstances_shouldReturnEmptyList() {

    when(serviceScope.getInstances()).thenReturn(Collections.emptyList());

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void collectMetrics_whenInstanceScopeHasNoInstances_shouldReturnEmptyList() {

    when(instanceScope.getInstances()).thenReturn(Collections.emptyList());

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void collectMetrics_whenServiceScopeHasNoMetrics_shouldReturnEmptyList() {

    when(serviceScope.getMetrics()).thenReturn(Collections.emptyList());

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertNotNull(results);
    assertTrue(results.isEmpty());
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

    provider.collectMetrics(accountScope, request);

    // 2 instances * 3 metrics = 6 GCP requests
    verify(client, times(6)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_shouldBuildCorrectProjectNameAndFilter() {

    configureClient();

    provider.collectMetrics(accountScope, request);

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

    provider.collectMetrics(accountScope, request);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals(ListTimeSeriesRequest.TimeSeriesView.FULL, captor.getValue().getView());
  }

  @Test
  void collectMetrics_shouldBuildCorrectTimeInterval() {

    configureClient();

    provider.collectMetrics(accountScope, request);

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

    provider.collectMetrics(accountScope, request);

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

    provider.collectMetrics(accountScope, request);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals(300, captor.getValue().getAggregation().getAlignmentPeriod().getSeconds());
  }

  @Test
  void collectMetrics_shouldUseConfiguredProjectId() {

    when(credentials.getProjectId()).thenReturn("different-project");

    configureClient();

    provider.collectMetrics(accountScope, request);

    ArgumentCaptor<ListTimeSeriesRequest> captor =
        ArgumentCaptor.forClass(ListTimeSeriesRequest.class);

    verify(client).listTimeSeries(captor.capture());

    assertEquals("projects/different-project", captor.getValue().getName());
  }

  @Test
  void collectMetrics_shouldPreserveDimensions() throws Exception {

    Point point = createDoublePoint(55.5, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    TimeSeries series =
        TimeSeries.newBuilder()
            .setResource(
                MonitoredResource.newBuilder()
                    .setType("gce_instance")
                    .putLabels("instance_id", "instance-123")
                    .putLabels("zone", "us-central1-a")
                    .putLabels("project_id", "test-project")
                    .build())
            .addPoints(point)
            .build();

    configureClient(series);

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());

    UsageRecordModel result = results.get(0);

    assertEquals("instance-123", result.getDimensions().get("instance_id"));

    assertEquals("us-central1-a", result.getDimensions().get("zone"));

    assertEquals("test-project", result.getDimensions().get("project_id"));
  }

  @Test
  void collectMetrics_shouldUseInstanceRegion() throws Exception {

    when(instance.getRegion()).thenReturn("europe-west1-b");

    Point point = createDoublePoint(25.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    configureClient(createTimeSeries("instance-123", "europe-west1-b", point));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());

    assertEquals("europe-west1-b", results.get(0).getRegion());
  }

  @Test
  void collectMetrics_shouldAssignSameIngestionIdToAllRecords() throws Exception {

    Point first = createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    Point second = createDoublePoint(20.0, "2026-09-02T12:02:00Z", "2026-09-02T12:01:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", first, second));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(2, results.size());

    assertNotNull(results.get(0).getIngestionId());

    assertEquals(results.get(0).getIngestionId(), results.get(1).getIngestionId());
  }

  @Test
  void collectMetrics_shouldGenerateUniqueRecordIds() throws Exception {

    Point first = createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    Point second = createDoublePoint(20.0, "2026-09-02T12:02:00Z", "2026-09-02T12:01:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", first, second));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(2, results.size());

    assertNotNull(results.get(0).getRecordId());
    assertNotNull(results.get(1).getRecordId());

    assertNotEquals(results.get(0).getRecordId(), results.get(1).getRecordId());
  }

  @Test
  void collectMetrics_shouldAssignGcpProvider() throws Exception {

    configureClient(
        createTimeSeries(
            "instance-123",
            "us-central1-a",
            createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z")));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());

    assertEquals("GCP", results.get(0).getProvider());

    assertEquals("GCPMonitoringService", results.get(0).getSource());
  }

  @Test
  void collectMetrics_shouldAssignAccountIdToAllRecords() throws Exception {

    Point first = createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z");

    Point second = createDoublePoint(20.0, "2026-09-02T12:02:00Z", "2026-09-02T12:01:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", first, second));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(2, results.size());

    assertEquals("account-123", results.get(0).getAccountId());

    assertEquals("account-123", results.get(1).getAccountId());
  }

  @Test
  void collectMetrics_shouldCloseClient() {

    configureClient();

    provider.collectMetrics(accountScope, request);

    verify(client).close();
  }

  @Test
  void
      collectMetrics_whenCredentialsFactoryThrowsIOException_shouldThrowIllegalArgumentException() {

    gcpClientFactory
        .when(() -> GcpClientFactory.credentials(credentials))
        .thenThrow(new IOException("invalid credentials"));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> provider.collectMetrics(accountScope, request));

    assertEquals(
        "Invalid account credentials provided for GCP usage metric ingestion",
        exception.getMessage());

    verifyNoInteractions(client);
  }

  @Test
  void
      collectMetrics_whenMetricServiceClientCreationThrowsIOException_shouldThrowIllegalArgumentException() {

    metricServiceClientStatic
        .when(() -> MetricServiceClient.create(any(MetricServiceSettings.class)))
        .thenThrow(new IOException("invalid credentials"));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> provider.collectMetrics(accountScope, request));

    assertEquals(
        "Invalid account credentials provided for GCP usage metric ingestion",
        exception.getMessage());
  }

  @Test
  void collectMetrics_shouldCallGcpCredentialsFactoryWithRequestCredentials() {

    configureClient();

    provider.collectMetrics(accountScope, request);

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

    provider.collectMetrics(accountScope, request);

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

    provider.collectMetrics(accountScope, request);

    verify(client, times(2)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_shouldBuildFilterUsingInstanceIdentifierName() {

    when(instanceScope.getIdentifierName()).thenReturn("resource_id");

    when(instance.getIdentifier()).thenReturn("resource-456");

    when(serviceScope.getName()).thenReturn("custom_resource");

    when(metric.getName()).thenReturn("custom.metric");

    configureClient();

    provider.collectMetrics(accountScope, request);

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

    provider.collectMetrics(accountScope, request);

    // One metric in the 1st service + one metric in the 2nd service
    verify(client, times(2)).listTimeSeries(any(ListTimeSeriesRequest.class));
  }

  @Test
  void collectMetrics_whenMultipleReturnedSeries_shouldPreserveAllPoints() throws Exception {

    TimeSeries firstSeries =
        createTimeSeries(
            "instance-123",
            "us-central1-a",
            createDoublePoint(10.0, "2026-09-02T12:01:00Z", "2026-09-02T12:00:00Z"),
            createDoublePoint(20.0, "2026-09-02T12:02:00Z", "2026-09-02T12:01:00Z"));

    TimeSeries secondSeries =
        createTimeSeries(
            "instance-123",
            "us-central1-a",
            createDoublePoint(30.0, "2026-09-02T12:03:00Z", "2026-09-02T12:02:00Z"),
            createDoublePoint(40.0, "2026-09-02T12:04:00Z", "2026-09-02T12:03:00Z"));

    configureClient(firstSeries, secondSeries);

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(4, results.size());

    assertEquals(10.0, results.get(0).getValue());
    assertEquals(20.0, results.get(1).getValue());
    assertEquals(30.0, results.get(2).getValue());
    assertEquals(40.0, results.get(3).getValue());
  }

  @Test
  void collectMetrics_shouldSetPeriodStartAndEndFromPointInterval() throws Exception {

    Point point = createDoublePoint(99.9, "2026-09-02T12:04:00Z", "2026-09-02T12:03:00Z");

    configureClient(createTimeSeries("instance-123", "us-central1-a", point));

    List<UsageRecordModel> results = provider.collectMetrics(accountScope, request);

    assertEquals(1, results.size());

    UsageRecordModel result = results.get(0);

    assertEquals(Instant.parse("2026-09-02T12:03:00Z"), result.getPeriodStart());

    assertEquals(Instant.parse("2026-09-02T12:04:00Z"), result.getPeriodEnd());

    assertEquals(Instant.parse("2026-09-02T12:04:00Z"), result.getTimestamp());
  }

  /**
   * Configures the mocked GCP client
   *
   * <p>The client returns Iterable<TimeSeries> which we recreate to mock the response. Different
   * Point types are simulated for testing
   */
  private void configureClient(TimeSeries... series) {
    MetricServiceClient.ListTimeSeriesPagedResponse response =
        mock(MetricServiceClient.ListTimeSeriesPagedResponse.class);

    when(response.iterateAll()).thenReturn(List.of(series));

    when(client.listTimeSeries(any(ListTimeSeriesRequest.class))).thenReturn(response);
  }

  private TimeSeries createTimeSeries(String instanceId, String zone, Point... points) {

    MonitoredResource resource =
        MonitoredResource.newBuilder()
            .setType("gce_instance")
            .putLabels("instance_id", instanceId)
            .putLabels("zone", zone)
            .build();

    return TimeSeries.newBuilder().setResource(resource).addAllPoints(List.of(points)).build();
  }

  private Point createDoublePoint(double value, String end, String start) throws ParseException {

    return Point.newBuilder()
        .setInterval(createInterval(start, end))
        .setValue(TypedValue.newBuilder().setDoubleValue(value).build())
        .build();
  }

  private Point createInt64Point(long value, String end, String start) throws ParseException {

    return Point.newBuilder()
        .setInterval(createInterval(start, end))
        .setValue(TypedValue.newBuilder().setInt64Value(value).build())
        .build();
  }

  private Point createBooleanPoint(boolean value, String end, String start) throws ParseException {

    return Point.newBuilder()
        .setInterval(createInterval(start, end))
        .setValue(TypedValue.newBuilder().setBoolValue(value).build())
        .build();
  }

  private TimeInterval createInterval(String start, String end) throws ParseException {

    return TimeInterval.newBuilder()
        .setStartTime(Timestamps.parse(start))
        .setEndTime(Timestamps.parse(end))
        .build();
  }
}
