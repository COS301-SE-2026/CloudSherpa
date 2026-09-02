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
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
  void setUp() throws Exception {
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
