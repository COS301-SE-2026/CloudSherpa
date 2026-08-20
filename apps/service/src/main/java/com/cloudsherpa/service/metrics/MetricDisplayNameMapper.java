package com.cloudsherpa.service.metrics;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MetricDisplayNameMapper {

  private static final Map<String, String> DISPLAY_NAMES =
      Map.ofEntries(
          Map.entry("CPUUtilization", "CPU Utilization"),
          Map.entry("NetworkIn", "Network In"),
          Map.entry("NetworkOut", "Network Out"),
          Map.entry("DiskReadBytes", "Disk Read Bytes"),
          Map.entry("DiskWriteBytes", "Disk Write Bytes"),
          Map.entry("MemoryUtilization", "Memory Utilization"),
          Map.entry("FreeableMemory", "Freeable Memory"),
          Map.entry("FreeStorageSpace", "Free Storage Space"),
          Map.entry("BucketSizeBytes", "Bucket Size"),
          Map.entry("NumberOfObjects", "Number of Objects"),
          Map.entry("Duration", "Duration"),
          Map.entry("ReadLatency", "Read Latency"),
          Map.entry("WriteLatency", "Write Latency"),
          Map.entry("FirstByteLatency", "First Byte Latency"),
          Map.entry("Errors", "Errors"),
          Map.entry("AllRequests", "Requests"),
          Map.entry("DatabaseConnections", "Database Connections"),
          Map.entry("Invocations", "Invocations"),
          Map.entry("Throttles", "Throttles"),
          Map.entry("WriteThrottleEvents", "Write Throttle Events"),
          Map.entry("ReadThrottleEvents", "Read Throttle Events"),
          Map.entry("ConsumedReadCapacityUnits", "Consumed Read Capacity"),
          Map.entry("ConsumedWriteCapacityUnits", "Consumed Write Capacity"));

  public String toDisplayName(String canonicalName) {
    return DISPLAY_NAMES.getOrDefault(canonicalName, canonicalName);
  }
}
