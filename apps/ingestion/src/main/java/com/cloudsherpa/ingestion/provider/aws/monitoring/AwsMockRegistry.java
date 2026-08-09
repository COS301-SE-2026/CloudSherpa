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
                metrics.throughput("NetworkPacketsIn", "Count"),
                metrics.throughput("NetworkPacketsOut", "Count"),
                metrics.counter("DiskReadOps", "Count"),
                metrics.counter("DiskWriteOps", "Count"),
                metrics.storage("DiskReadBytes", "Bytes"),
                metrics.storage("DiskWriteBytes", "Bytes"),
                metrics.counter("StatusCheckFailed", "Count"),
                metrics.counter("StatusCheckFailed_Instance", "Count"),
                metrics.counter("StatusCheckFailed_System", "Count"))
            .register()
            .service("AWS/LAMBDA")
            .baseLoad(5)
            .variance(80)
            .burstChance(.12)
            .metrics(
                metrics.counter("Invocations", "Count"),
                metrics.counter("Errors", "Count"),
                metrics.latency("Duration", "Milliseconds"),
                metrics.counter("Throttles", "Count"),
                metrics.latency("IteratorAge", "Milliseconds"),
                metrics.counter("ConcurrentExecutions", "Count"),
                metrics.counter("UnreservedConcurrentExecutions", "Count"))
            .register()
            .service("AWS/RDS")
            .baseLoad(20)
            .variance(40)
            .burstChance(.02)
            .metrics(
                metrics.cpu("CPUUtilization"),
                metrics.counter("DatabaseConnections", "Count"),
                metrics.storage("FreeStorageSpace", "Bytes"),
                metrics.latency("ReadLatency", "Milliseconds"),
                metrics.latency("WriteLatency", "Milliseconds"),
                metrics.throughput("ReadIOPS", "Count/Second"),
                metrics.throughput("WriteIOPS", "Count/Second"),
                metrics.throughput("NetworkReceiveThroughput", "Bytes/Second"),
                metrics.throughput("NetworkTransmitThroughput", "Bytes/Second"),
                metrics.storage("FreeableMemory", "Bytes"),
                metrics.storage("SwapUsage", "Bytes"))
            .register()
            .service("AWS/S3")
            .baseLoad(2)
            .variance(20)
            .burstChance(.01)
            .metrics(
                metrics.counter("NumberOfObjects", "Count"),
                metrics.storage("BucketSizeBytes", "Bytes"),
                metrics.counter("AllRequests", "Count"),
                metrics.counter("GetRequests", "Count"),
                metrics.counter("PutRequests", "Count"),
                metrics.counter("DeleteRequests", "Count"),
                metrics.counter("4xxErrors", "Count"),
                metrics.counter("5xxErrors", "Count"),
                metrics.latency("FirstByteLatency", "Milliseconds"),
                metrics.latency("TotalRequestLatency", "Milliseconds"))
            .register()
            .service("AWS/DYNAMODB")
            .baseLoad(10)
            .variance(60)
            .burstChance(.08)
            .metrics(
                metrics.counter("ConsumedReadCapacityUnits", "Count"),
                metrics.counter("ConsumedWriteCapacityUnits", "Count"),
                metrics.counter("ReadThrottleEvents", "Count"),
                metrics.counter("WriteThrottleEvents", "Count"),
                metrics.counter("ThrottledRequests", "Count"),
                metrics.latency("SuccessfulRequestLatency", "Milliseconds"),
                metrics.counter("SystemErrors", "Count"),
                metrics.counter("UserErrors", "Count"))
            .register()
            .service("AWS/ECS")
            .baseLoad(25)
            .variance(70)
            .burstChance(.05)
            .metrics(
                metrics.cpu("CPUUtilization"),
                metrics.cpu("MemoryUtilization"),
                metrics.counter("RunningTaskCount", "Count"),
                metrics.counter("PendingTaskCount", "Count"),
                metrics.counter("ServiceCount", "Count"))
            .register()
            .service("AWS/EKS")
            .baseLoad(25)
            .variance(70)
            .burstChance(.05)
            .metrics(
                metrics.cpu("CPUUtilization"),
                metrics.cpu("MemoryUtilization"),
                metrics.counter("RunningTaskCount", "Count"),
                metrics.counter("PendingTaskCount", "Count"),
                metrics.counter("ServiceCount", "Count"))
            .register()
            .service("AWS/GPU")
            .baseLoad(15)
            .variance(90)
            .burstChance(.02)
            .metrics(
                metrics.cpu("GPUUtilization"),
                metrics.cpu("GPUMemoryUtilization"),
                metrics.cpu("CPUUtilization"),
                metrics.cpu("MemoryUtilization"),
                metrics.cpu("DiskUtilization"),
                metrics.counter("TrainingLoss", "None"),
                metrics.counter("BatchSize", "Count"),
                metrics.latency("IterationTime", "Milliseconds"))
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
