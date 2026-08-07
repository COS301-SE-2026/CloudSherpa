package com.cloudsherpa.ingestion.provider.aws.monitoring;

import com.cloudsherpa.ingestion.provider.mock.builder.MockRegistryBuilder;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.factory.MetricDefinitionFactory;
import com.cloudsherpa.ingestion.provider.mock.registry.MockMetricRegistry;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class AwsMockRegistry implements MockMetricRegistry {

  private final MockMetricRegistry registry;

  public AwsMockRegistry(MetricDefinitionFactory metrics) {

    registry =
        new MockRegistryBuilder()
            .service("AWS/EC2")
            .baseLoad(30)
            .variance(50)
            .burstChance(.03)
            .metrics(
                metrics.cpu("CPUUtilization"),
                metrics.throughput("NetworkIn", "Bytes"),
                metrics.throughput("NetworkOut", "Bytes"),
                metrics.counter("DiskReadOps", "Count"),
                metrics.counter("DiskWriteOps", "Count"),
                metrics.storage("DiskReadBytes", "Bytes"),
                metrics.storage("DiskWriteBytes", "Bytes"))
            .register()
            .service("AWS/RDS")
            .baseLoad(20)
            .variance(40)
            .burstChance(.02)
            .metrics(
                metrics.cpu("CPUUtilization"),
                metrics.counter("DatabaseConnections", "Count"),
                metrics.latency("ReadLatency", "Milliseconds"),
                metrics.latency("WriteLatency", "Milliseconds"))
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
