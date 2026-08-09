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

  public GcpMockRegistry(MetricDefinitionFactory metrics) {

    registry =
        new MockRegistryBuilder()
            .service("gce_instance")
            .baseLoad(30)
            .variance(50)
            .burstChance(.03)
            .metrics(
                metrics.cpu("compute.googleapis.com/instance/cpu/utilization", "10^2.%"),
                metrics.counter("compute.googleapis.com/instance/cpu/reserved_cores", "Count"),
                metrics.throughput(
                    "compute.googleapis.com/instance/network/received_bytes_count", "By"),
                metrics.throughput(
                    "compute.googleapis.com/instance/network/sent_bytes_count", "By"),
                metrics.storage("compute.googleapis.com/instance/disk/read_bytes_count", "By"),
                metrics.storage("compute.googleapis.com/instance/disk/write_bytes_count", "By"),
                metrics.counter("compute.googleapis.com/instance/disk/read_ops_count", "Count"),
                metrics.counter("compute.googleapis.com/instance/disk/write_ops_count", "Count"))
            .register()
            .service("cloudsql_database")
            .baseLoad(35)
            .variance(40)
            .burstChance(.02)
            .metrics(
                metrics.cpu("cloudsql.googleapis.com/database/cpu/utilization", "10^2.%"),
                metrics.counter("cloudsql.googleapis.com/database/cpu/reserved_cores", "Count"),
                metrics.storage("cloudsql.googleapis.com/database/disk/bytes_used", "By"),
                metrics.storage("cloudsql.googleapis.com/database/disk/quota", "By"),
                metrics.counter("cloudsql.googleapis.com/database/network/connections", "Count"),
                metrics.throughput(
                    "cloudsql.googleapis.com/database/network/received_bytes_count", "By"),
                metrics.throughput(
                    "cloudsql.googleapis.com/database/network/sent_bytes_count", "By"))
            .register()
            .service("gcs_bucket")
            .baseLoad(25)
            .variance(35)
            .burstChance(.02)
            .metrics(
                metrics.storage("storage.googleapis.com/storage/total_bytes", "By"),
                metrics.counter("storage.googleapis.com/storage/object_count", "Count"),
                metrics.throughput("storage.googleapis.com/network/received_bytes_count", "By"),
                metrics.throughput("storage.googleapis.com/network/sent_bytes_count", "By"),
                metrics.counter("storage.googleapis.com/api/lro_count", "Count"))
            .register()
            .service("pubsub_subscription")
            .baseLoad(40)
            .variance(45)
            .burstChance(.04)
            .metrics(
                metrics.counter(
                    "pubsub.googleapis.com/subscription/num_undelivered_messages", "Count"),
                metrics.throughput("pubsub.googleapis.com/subscription/byte_cost", "By"),
                metrics.counter("pubsub.googleapis.com/subscription/ack_message_count", "Count"),
                metrics.counter(
                    "pubsub.googleapis.com/subscription/pull_ack_message_operation_count", "Count"))
            .register()
            .service("pubsub_topic")
            .baseLoad(40)
            .variance(45)
            .burstChance(.04)
            .metrics(
                metrics.counter(
                    "pubsub.googleapis.com/topic/send_message_operation_count", "Count"),
                metrics.throughput("pubsub.googleapis.com/topic/byte_cost", "By"))
            .register()
            .service("cloud_run_revision")
            .baseLoad(30)
            .variance(50)
            .burstChance(.03)
            .metrics(
                metrics.counter("run.googleapis.com/request_count", "Count"),
                metrics.cpu("run.googleapis.com/container/cpu/utilizations", "10^2.%"),
                metrics.storage("run.googleapis.com/container/memory/used_bytes", "By"),
                metrics.throughput(
                    "run.googleapis.com/container/network/received_bytes_count", "By"),
                metrics.throughput("run.googleapis.com/container/network/sent_bytes_count", "By"))
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
