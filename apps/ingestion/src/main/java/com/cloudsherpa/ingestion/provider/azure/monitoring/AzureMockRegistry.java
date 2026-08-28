package com.cloudsherpa.ingestion.provider.azure.monitoring;

import com.cloudsherpa.ingestion.provider.mock.builder.MockRegistryBuilder;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.factory.MetricDefinitionFactory;
import com.cloudsherpa.ingestion.provider.mock.registry.MockMetricRegistry;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class AzureMockRegistry implements MockMetricRegistry {

  private static final String PERCENT = "Percent";
  private static final String BYTES = "Bytes";
  private static final String BYTES_PER_SECOND = "BytesPerSecond";
  private static final String COUNT = "Count";

  private final MockMetricRegistry registry;

  public AzureMockRegistry(MetricDefinitionFactory metrics) {
    registry =
        new MockRegistryBuilder()
            .service("Microsoft.Compute/virtualMachines")
            .baseLoad(35)
            .variance(35)
            .burstChance(.05)
            .metrics(
                metrics.cpu("Percentage CPU", PERCENT),
                metrics.throughput("Network In Total", BYTES),
                metrics.throughput("Network Out Total", BYTES),
                metrics.throughput("OS Disk Read Bytes/sec", BYTES_PER_SECOND),
                metrics.throughput("OS Disk Write Bytes/sec", BYTES_PER_SECOND))
            .register()
            .service("Microsoft.Sql/servers/databases")
            .baseLoad(30)
            .variance(40)
            .burstChance(.04)
            .metrics(
                metrics.cpu("cpu_percent", PERCENT),
                metrics.storage("allocated_data_storage", BYTES),
                metrics.counter("connection_successful", COUNT),
                metrics.counter("deadlock", COUNT),
                metrics.cpu("dtu_consumption_percent", PERCENT))
            .register()
            .service("Microsoft.Storage/storageAccounts/blobServices")
            .baseLoad(25)
            .variance(35)
            .burstChance(.03)
            .metrics(
                metrics.storage("BlobCapacity", BYTES),
                metrics.counter("BlobCount", COUNT),
                metrics.percentage("Availability", PERCENT),
                metrics.throughput("Ingress", BYTES),
                metrics.throughput("Egress", BYTES))
            .register()
            .service("Microsoft.Web/sites")
            .baseLoad(25)
            .variance(45)
            .burstChance(.06)
            .metrics(
                metrics.counter("Requests", COUNT),
                metrics.counter("Http2xx", COUNT),
                metrics.counter("Http5xx", COUNT),
                metrics.latency("HttpResponseTime", "Seconds"),
                metrics.storage("MemoryWorkingSet", BYTES))
            .register()
            .build();
  }

  @Override
  public MockServiceDefinition service(String name) {
    return registry.service(name);
  }

  @Override
  public boolean contains(String name) {
    return registry.contains(name);
  }

  @Override
  public Collection<MockServiceDefinition> services() {
    return registry.services();
  }
}
