package com.cloudsherpa.ingestion.provider.gcp.monitoring;

import com.cloudsherpa.ingestion.provider.mock.builder.MockRegistryBuilder;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.factory.MetricDefinitionFactory;
import com.cloudsherpa.ingestion.provider.mock.registry.MockMetricRegistry;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class GcpMockRegistry implements MockMetricRegistry {

  private final MockMetricRegistry registry;

  private static final String COUNT = "Count";
  private static final String PERCENT = "10^2.%";
  private static final String BYTES = "By";

  public GcpMockRegistry(MetricDefinitionFactory metrics) {

    registry =
        new MockRegistryBuilder()
            .service("gce_instance")
            .baseLoad(30)
            .variance(50)
            .burstChance(.03)
            .metrics(
                metrics.cpu("compute.googleapis.com/instance/cpu/utilization", PERCENT),
                metrics.counter("compute.googleapis.com/instance/cpu/reserved_cores", COUNT),
                metrics.throughput(
                    "compute.googleapis.com/instance/network/received_bytes_count", BYTES),
                metrics.throughput(
                    "compute.googleapis.com/instance/network/sent_bytes_count", BYTES),
                metrics.storage("compute.googleapis.com/instance/disk/read_bytes_count", BYTES),
                metrics.storage("compute.googleapis.com/instance/disk/write_bytes_count", BYTES),
                metrics.counter("compute.googleapis.com/instance/disk/read_ops_count", COUNT),
                metrics.counter("compute.googleapis.com/instance/disk/write_ops_count", COUNT))
            .register()
            .service("cloudsql_database")
            .baseLoad(35)
            .variance(40)
            .burstChance(.02)
            .metrics(
                metrics.cpu("cloudsql.googleapis.com/database/cpu/utilization", PERCENT),
                metrics.counter("cloudsql.googleapis.com/database/cpu/reserved_cores", COUNT),
                metrics.storage("cloudsql.googleapis.com/database/disk/bytes_used", BYTES),
                metrics.storage("cloudsql.googleapis.com/database/disk/quota", BYTES),
                metrics.counter("cloudsql.googleapis.com/database/network/connections", COUNT),
                metrics.throughput(
                    "cloudsql.googleapis.com/database/network/received_bytes_count", BYTES),
                metrics.throughput(
                    "cloudsql.googleapis.com/database/network/sent_bytes_count", BYTES))
            .register()
            .service("gcs_bucket")
            .baseLoad(25)
            .variance(35)
            .burstChance(.02)
            .metrics(
                metrics.storage("storage.googleapis.com/storage/total_bytes", BYTES),
                metrics.counter("storage.googleapis.com/storage/object_count", COUNT),
                metrics.throughput("storage.googleapis.com/network/received_bytes_count", BYTES),
                metrics.throughput("storage.googleapis.com/network/sent_bytes_count", BYTES),
                metrics.counter("storage.googleapis.com/api/lro_count", COUNT))
            .register()
            .service("pubsub_subscription")
            .baseLoad(40)
            .variance(45)
            .burstChance(.04)
            .metrics(
                metrics.counter(
                    "pubsub.googleapis.com/subscription/num_undelivered_messages", COUNT),
                metrics.throughput("pubsub.googleapis.com/subscription/byte_cost", BYTES),
                metrics.counter("pubsub.googleapis.com/subscription/ack_message_count", COUNT),
                metrics.counter(
                    "pubsub.googleapis.com/subscription/pull_ack_message_operation_count", COUNT))
            .register()
            .service("pubsub_topic")
            .baseLoad(40)
            .variance(45)
            .burstChance(.04)
            .metrics(
                metrics.counter("pubsub.googleapis.com/topic/send_message_operation_count", COUNT),
                metrics.throughput("pubsub.googleapis.com/topic/byte_cost", BYTES))
            .register()
            .service("cloud_run_revision")
            .baseLoad(30)
            .variance(50)
            .burstChance(.03)
            .metrics(
                metrics.counter("run.googleapis.com/request_count", COUNT),
                metrics.cpu("run.googleapis.com/container/cpu/utilizations", PERCENT),
                metrics.storage("run.googleapis.com/container/memory/used_bytes", BYTES),
                metrics.throughput(
                    "run.googleapis.com/container/network/received_bytes_count", BYTES),
                metrics.throughput("run.googleapis.com/container/network/sent_bytes_count", BYTES))
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
