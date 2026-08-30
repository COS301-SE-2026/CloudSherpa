package com.cloudsherpa.service.metrics;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MetricDisplayNameMapper {

  // Canonical display names shared across providers.
  public static final String CPU_UTILIZATION = "CPU Utilization";
  public static final String NETWORK_IN = "Network In";
  public static final String NETWORK_OUT = "Network Out";
  public static final String DISK_READ_BYTES = "Disk Read Bytes";
  public static final String DISK_WRITE_BYTES = "Disk Write Bytes";
  public static final String STATUS_CHECK_FAILED = "Status Check Failed";
  public static final String MEMORY_UTILIZATION = "Memory Utilization";
  public static final String CPU_RESERVATION = "CPU Reservation";
  public static final String MEMORY_RESERVATION = "Memory Reservation";
  public static final String CLUSTER_FAILED_REQUEST_COUNT = "Cluster Failed Request Count";
  public static final String CLUSTER_NODE_COUNT = "Cluster Node Count";
  public static final String CLUSTER_REQUEST_TOTAL = "Cluster Request Total";
  public static final String INVOCATIONS = "Invocations";
  public static final String ERRORS = "Errors";
  public static final String DURATION = "Duration";
  public static final String CONCURRENT_EXECUTIONS = "Concurrent Executions";
  public static final String THROTTLES = "Throttles";
  public static final String DATABASE_CONNECTIONS = "Database Connections";
  public static final String FREE_STORAGE_SPACE = "Free Storage Space";
  public static final String READ_LATENCY = "Read Latency";
  public static final String WRITE_LATENCY = "Write Latency";
  public static final String FREEABLE_MEMORY = "Freeable Memory";
  public static final String CURRENT_CONNECTIONS = "Current Connections";
  public static final String EVICTIONS = "Evictions";
  public static final String NETWORK_BYTES_IN = "Network Bytes In";
  public static final String NETWORK_BYTES_OUT = "Network Bytes Out";
  public static final String JVM_MEMORY_PRESSURE = "JVM Memory Pressure";
  public static final String CLUSTER_INDEX_WRITES_BLOCKED = "Cluster Index Writes Blocked";
  public static final String SEARCH_LATENCY = "Search Latency";
  public static final String HEALTH_STATUS = "Health Status";
  public static final String PERCENTAGE_DISK_SPACE_USED = "Percentage Disk Space Used";
  public static final String READ_IOPS = "Read IOPS";
  public static final String WRITE_IOPS = "Write IOPS";
  public static final String RESERVED_CPU_CORES = "Reserved CPU cores";
  public static final String CPU_USAGE_TIME = "CPU usage time";
  public static final String MEMORY_USED = "Memory used";
  public static final String POD_RESTART_COUNT = "Pod restart count";
  public static final String FUNCTION_EXECUTIONS = "Function executions";
  public static final String FUNCTION_EXECUTION_TIME = "Function execution time";
  public static final String MEMORY_USAGE = "Memory usage";
  public static final String ACTIVE_INSTANCES = "Active instances";
  public static final String HTTP_REQUESTS = "HTTP requests";
  public static final String REQUEST_LATENCY = "Request latency";
  public static final String RUNNING_INSTANCES = "Running instances";
  public static final String STORED_BYTES = "Stored bytes";
  public static final String API_REQUESTS = "API requests";
  public static final String BYTES_UPLOADED = "Bytes uploaded";
  public static final String BYTES_DOWNLOADED = "Bytes downloaded";

  private static final Map<String, String> AWS_DISPLAY_NAMES =
      Map.ofEntries(
          Map.entry("CPUUtilization", CPU_UTILIZATION),
          Map.entry("NetworkIn", NETWORK_IN),
          Map.entry("NetworkOut", NETWORK_OUT),
          Map.entry("DiskReadBytes", DISK_READ_BYTES),
          Map.entry("DiskWriteBytes", DISK_WRITE_BYTES),
          Map.entry("StatusCheckFailed", STATUS_CHECK_FAILED),
          Map.entry("MemoryUtilization", MEMORY_UTILIZATION),
          Map.entry("CPUReservation", CPU_RESERVATION),
          Map.entry("MemoryReservation", MEMORY_RESERVATION),
          Map.entry("cluster_failed_request_count", CLUSTER_FAILED_REQUEST_COUNT),
          Map.entry("cluster_node_count", CLUSTER_NODE_COUNT),
          Map.entry("cluster_request_total", CLUSTER_REQUEST_TOTAL),
          Map.entry(INVOCATIONS, INVOCATIONS),
          Map.entry(ERRORS, ERRORS),
          Map.entry(DURATION, DURATION),
          Map.entry("ConcurrentExecutions", CONCURRENT_EXECUTIONS),
          Map.entry(THROTTLES, THROTTLES),
          Map.entry("DatabaseConnections", DATABASE_CONNECTIONS),
          Map.entry("FreeStorageSpace", FREE_STORAGE_SPACE),
          Map.entry("ReadLatency", READ_LATENCY),
          Map.entry("WriteLatency", WRITE_LATENCY),
          Map.entry("FreeableMemory", FREEABLE_MEMORY),
          Map.entry("CurrConnections", CURRENT_CONNECTIONS),
          Map.entry(EVICTIONS, EVICTIONS),
          Map.entry("NetworkBytesIn", NETWORK_BYTES_IN),
          Map.entry("NetworkBytesOut", NETWORK_BYTES_OUT),
          Map.entry("JVMMemoryPressure", JVM_MEMORY_PRESSURE),
          Map.entry("ClusterIndexWritesBlocked", CLUSTER_INDEX_WRITES_BLOCKED),
          Map.entry("SearchLatency", SEARCH_LATENCY),
          Map.entry("HealthStatus", HEALTH_STATUS),
          Map.entry("PercentageDiskSpaceUsed", PERCENTAGE_DISK_SPACE_USED),
          Map.entry("ReadIOPS", READ_IOPS),
          Map.entry("WriteIOPS", WRITE_IOPS));

  // GCP metrics from offered_metric
  private static final Map<String, String> GCP_DISPLAY_NAMES =
      Map.ofEntries(
          Map.entry("compute.googleapis.com/instance/cpu/utilization", CPU_UTILIZATION),
          Map.entry("compute.googleapis.com/instance/cpu/reserved_cores", RESERVED_CPU_CORES),
          Map.entry("compute.googleapis.com/instance/network/received_bytes_count", NETWORK_IN),
          Map.entry("compute.googleapis.com/instance/network/sent_bytes_count", NETWORK_OUT),
          Map.entry("compute.googleapis.com/instance/disk/read_bytes_count", DISK_READ_BYTES),
          Map.entry("compute.googleapis.com/instance/disk/write_bytes_count", DISK_WRITE_BYTES),
          Map.entry("compute.googleapis.com/instance/disk/read_ops_count", READ_IOPS),
          Map.entry("compute.googleapis.com/instance/disk/write_ops_count", WRITE_IOPS),
          Map.entry("kubernetes.io/node/cpu/core_usage_time", CPU_USAGE_TIME),
          Map.entry("kubernetes.io/node/memory/used_bytes", MEMORY_USED),
          Map.entry("kubernetes.io/node/network/received_bytes_count", NETWORK_IN),
          Map.entry("kubernetes.io/node/network/sent_bytes_count", NETWORK_OUT),
          Map.entry("kubernetes.io/pod/restart_count", POD_RESTART_COUNT),
          Map.entry("cloudfunctions.googleapis.com/function/execution_count", FUNCTION_EXECUTIONS),
          Map.entry(
              "cloudfunctions.googleapis.com/function/execution_times", FUNCTION_EXECUTION_TIME),
          Map.entry("cloudfunctions.googleapis.com/function/user_memory_bytes", MEMORY_USAGE),
          Map.entry("cloudfunctions.googleapis.com/function/active_instances", ACTIVE_INSTANCES),
          Map.entry("run.googleapis.com/request_count", HTTP_REQUESTS),
          Map.entry("run.googleapis.com/request_latencies", REQUEST_LATENCY),
          Map.entry("run.googleapis.com/container/cpu/utilizations", CPU_UTILIZATION),
          Map.entry("run.googleapis.com/container/memory/utilizations", MEMORY_UTILIZATION),
          Map.entry("run.googleapis.com/container/instance_count", RUNNING_INSTANCES),
          Map.entry("storage.googleapis.com/storage/total_bytes", STORED_BYTES),
          Map.entry("storage.googleapis.com/api/request_count", API_REQUESTS),
          Map.entry("storage.googleapis.com/network/received_bytes_count", BYTES_UPLOADED),
          Map.entry("storage.googleapis.com/network/sent_bytes_count", BYTES_DOWNLOADED));

  public String toDisplayName(String canonicalName) {
    if (AWS_DISPLAY_NAMES.containsKey(canonicalName)) {
      return AWS_DISPLAY_NAMES.get(canonicalName);
    }
    return GCP_DISPLAY_NAMES.getOrDefault(canonicalName, canonicalName);
  }

  private static final Map<String, String> AWS_CANONICAL_NAMES =
      AWS_DISPLAY_NAMES.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (e, r) -> e));

  private static final Map<String, String> GCP_CANONICAL_NAMES =
      GCP_DISPLAY_NAMES.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (e, r) -> e));

  // New provider-aware reverse lookup
  public String toCanonicalName(String provider, String displayName) {
    if ("AWS".equalsIgnoreCase(provider)) {
      return AWS_CANONICAL_NAMES.getOrDefault(displayName, displayName);
    } else if ("GCP".equalsIgnoreCase(provider)) {
      return GCP_CANONICAL_NAMES.getOrDefault(displayName, displayName);
    }
    return displayName;
  }
}
