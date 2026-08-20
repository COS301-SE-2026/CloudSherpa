package com.cloudsherpa.service.metrics;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MetricDisplayNameMapper {

  private static final Map<String, String> DISPLAY_NAMES =
      Map.ofEntries(
          // AWS metrics from offered_metric
          Map.entry("CPUUtilization", "CPU Utilization"),
          Map.entry("NetworkIn", "Network In"),
          Map.entry("NetworkOut", "Network Out"),
          Map.entry("DiskReadBytes", "Disk Read Bytes"),
          Map.entry("DiskWriteBytes", "Disk Write Bytes"),
          Map.entry("StatusCheckFailed", "Status Check Failed"),
          Map.entry("MemoryUtilization", "Memory Utilization"),
          Map.entry("CPUReservation", "CPU Reservation"),
          Map.entry("MemoryReservation", "Memory Reservation"),
          Map.entry("cluster_failed_request_count", "Cluster Failed Request Count"),
          Map.entry("cluster_node_count", "Cluster Node Count"),
          Map.entry("cluster_request_total", "Cluster Request Total"),
          Map.entry("Invocations", "Invocations"),
          Map.entry("Errors", "Errors"),
          Map.entry("Duration", "Duration"),
          Map.entry("ConcurrentExecutions", "Concurrent Executions"),
          Map.entry("Throttles", "Throttles"),
          Map.entry("DatabaseConnections", "Database Connections"),
          Map.entry("FreeStorageSpace", "Free Storage Space"),
          Map.entry("ReadLatency", "Read Latency"),
          Map.entry("WriteLatency", "Write Latency"),
          Map.entry("FreeableMemory", "Freeable Memory"),
          Map.entry("CurrConnections", "Current Connections"),
          Map.entry("Evictions", "Evictions"),
          Map.entry("NetworkBytesIn", "Network Bytes In"),
          Map.entry("NetworkBytesOut", "Network Bytes Out"),
          Map.entry("JVMMemoryPressure", "JVM Memory Pressure"),
          Map.entry("ClusterIndexWritesBlocked", "Cluster Index Writes Blocked"),
          Map.entry("SearchLatency", "Search Latency"),
          Map.entry("HealthStatus", "Health Status"),
          Map.entry("PercentageDiskSpaceUsed", "Percentage Disk Space Used"),
          Map.entry("ReadIOPS", "Read IOPS"),
          Map.entry("WriteIOPS", "Write IOPS"),

          // GCP metrics from offered_metric
          Map.entry("compute.googleapis.com/instance/cpu/utilization", "CPU utilization"),
          Map.entry("compute.googleapis.com/instance/cpu/reserved_cores", "Reserved CPU cores"),
          Map.entry(
              "compute.googleapis.com/instance/network/received_bytes_count",
              "Network bytes received"),
          Map.entry(
              "compute.googleapis.com/instance/network/sent_bytes_count", "Network bytes sent"),
          Map.entry("compute.googleapis.com/instance/disk/read_bytes_count", "Disk bytes read"),
          Map.entry("compute.googleapis.com/instance/disk/write_bytes_count", "Disk bytes written"),
          Map.entry("compute.googleapis.com/instance/disk/read_ops_count", "Disk read operations"),
          Map.entry(
              "compute.googleapis.com/instance/disk/write_ops_count", "Disk write operations"),
          Map.entry("kubernetes.io/node/cpu/core_usage_time", "CPU usage time"),
          Map.entry("kubernetes.io/node/memory/used_bytes", "Memory used"),
          Map.entry("kubernetes.io/node/network/received_bytes_count", "Network bytes received"),
          Map.entry("kubernetes.io/node/network/sent_bytes_count", "Network bytes sent"),
          Map.entry("kubernetes.io/pod/restart_count", "Pod restart count"),
          Map.entry(
              "cloudfunctions.googleapis.com/function/execution_count", "Function executions"),
          Map.entry(
              "cloudfunctions.googleapis.com/function/execution_times", "Function execution time"),
          Map.entry("cloudfunctions.googleapis.com/function/user_memory_bytes", "Memory usage"),
          Map.entry("cloudfunctions.googleapis.com/function/active_instances", "Active instances"),
          Map.entry("run.googleapis.com/request_count", "HTTP requests"),
          Map.entry("run.googleapis.com/request_latencies", "Request latency"),
          Map.entry("run.googleapis.com/container/cpu/utilizations", "CPU utilization"),
          Map.entry("run.googleapis.com/container/memory/utilizations", "Memory utilization"),
          Map.entry("run.googleapis.com/container/instance_count", "Running instances"),
          Map.entry("storage.googleapis.com/storage/total_bytes", "Stored bytes"),
          Map.entry("storage.googleapis.com/api/request_count", "API requests"),
          Map.entry("storage.googleapis.com/network/received_bytes_count", "Bytes uploaded"),
          Map.entry("storage.googleapis.com/network/sent_bytes_count", "Bytes downloaded"));

  public String toDisplayName(String canonicalName) {
    return DISPLAY_NAMES.getOrDefault(canonicalName, canonicalName);
  }
}
