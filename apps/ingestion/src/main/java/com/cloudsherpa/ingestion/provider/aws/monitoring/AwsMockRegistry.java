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

  private static final String CPU_UTILIZATION = "CPUUtilization";
  private static final String MEMORY_UTILIZATION = "MemoryUtilization";
  private static final String BYTES = "Bytes";
  private static final String COUNT = "Count";
  private static final String MILLISECONDS = "Milliseconds";

  public AwsMockRegistry(MetricDefinitionFactory metrics) {

    registry =
        new MockRegistryBuilder()
            .service("AWS/EC2")
            .baseLoad(30)
            .variance(50)
            .burstChance(.03)
            .metrics(
                metrics.cpu(CPU_UTILIZATION),
                metrics.throughput("NetworkIn", BYTES),
                metrics.throughput("NetworkOut", BYTES),
                metrics.throughput("NetworkPacketsIn", COUNT),
                metrics.throughput("NetworkPacketsOut", COUNT),
                metrics.counter("DiskReadOps", COUNT),
                metrics.counter("DiskWriteOps", COUNT),
                metrics.storage("DiskReadBytes", BYTES),
                metrics.storage("DiskWriteBytes", BYTES),
                metrics.counter("StatusCheckFailed", COUNT),
                metrics.counter("StatusCheckFailed_Instance", COUNT),
                metrics.counter("StatusCheckFailed_System", COUNT))
            .register()
            .service("AWS/LAMBDA")
            .baseLoad(5)
            .variance(80)
            .burstChance(.12)
            .metrics(
                metrics.counter("Invocations", COUNT),
                metrics.counter("Errors", COUNT),
                metrics.latency("Duration", MILLISECONDS),
                metrics.counter("Throttles", COUNT),
                metrics.latency("IteratorAge", MILLISECONDS),
                metrics.counter("ConcurrentExecutions", COUNT),
                metrics.counter("UnreservedConcurrentExecutions", COUNT))
            .register()
            .service("AWS/RDS")
            .baseLoad(20)
            .variance(40)
            .burstChance(.02)
            .metrics(
                metrics.cpu(CPU_UTILIZATION),
                metrics.counter("DatabaseConnections", COUNT),
                metrics.storage("FreeStorageSpace", BYTES),
                metrics.latency("ReadLatency", MILLISECONDS),
                metrics.latency("WriteLatency", MILLISECONDS),
                metrics.throughput("ReadIOPS", "Count/Second"),
                metrics.throughput("WriteIOPS", "Count/Second"),
                metrics.throughput("NetworkReceiveThroughput", "Bytes/Second"),
                metrics.throughput("NetworkTransmitThroughput", "Bytes/Second"),
                metrics.storage("FreeableMemory", BYTES),
                metrics.storage("SwapUsage", BYTES))
            .register()
            .service("AWS/S3")
            .baseLoad(2)
            .variance(20)
            .burstChance(.01)
            .metrics(
                metrics.counter("NumberOfObjects", COUNT),
                metrics.storage("BucketSizeBytes", BYTES),
                metrics.counter("AllRequests", COUNT),
                metrics.counter("GetRequests", COUNT),
                metrics.counter("PutRequests", COUNT),
                metrics.counter("DeleteRequests", COUNT),
                metrics.counter("4xxErrors", COUNT),
                metrics.counter("5xxErrors", COUNT),
                metrics.latency("FirstByteLatency", MILLISECONDS),
                metrics.latency("TotalRequestLatency", MILLISECONDS))
            .register()
            .service("AWS/DYNAMODB")
            .baseLoad(10)
            .variance(60)
            .burstChance(.08)
            .metrics(
                metrics.counter("ConsumedReadCapacityUnits", COUNT),
                metrics.counter("ConsumedWriteCapacityUnits", COUNT),
                metrics.counter("ReadThrottleEvents", COUNT),
                metrics.counter("WriteThrottleEvents", COUNT),
                metrics.counter("ThrottledRequests", COUNT),
                metrics.latency("SuccessfulRequestLatency", MILLISECONDS),
                metrics.counter("SystemErrors", COUNT),
                metrics.counter("UserErrors", COUNT))
            .register()
            .service("AWS/ECS")
            .baseLoad(25)
            .variance(70)
            .burstChance(.05)
            .metrics(
                metrics.cpu(CPU_UTILIZATION),
                metrics.cpu(MEMORY_UTILIZATION),
                metrics.counter("RunningTaskCount", COUNT),
                metrics.counter("PendingTaskCount", COUNT),
                metrics.counter("ServiceCount", COUNT))
            .register()
            .service("AWS/EKS")
            .baseLoad(25)
            .variance(70)
            .burstChance(.05)
            .metrics(
                metrics.cpu(CPU_UTILIZATION),
                metrics.cpu(MEMORY_UTILIZATION),
                metrics.counter("RunningTaskCount", COUNT),
                metrics.counter("PendingTaskCount", COUNT),
                metrics.counter("ServiceCount", COUNT))
            .register()
            .service("AWS/GPU")
            .baseLoad(15)
            .variance(90)
            .burstChance(.02)
            .metrics(
                metrics.cpu("GPUUtilization"),
                metrics.cpu("GPUMemoryUtilization"),
                metrics.cpu(CPU_UTILIZATION),
                metrics.cpu(MEMORY_UTILIZATION),
                metrics.cpu("DiskUtilization"),
                metrics.counter("TrainingLoss", "None"),
                metrics.counter("BatchSize", COUNT),
                metrics.latency("IterationTime", MILLISECONDS))
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
