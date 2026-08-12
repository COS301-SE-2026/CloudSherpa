package com.cloudsherpa.service.intelligence.service;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
  private List<Long> clusterValues;

  public Cluster() {
    clusterValues = new ArrayList<>();
  }

  public long getClusterMedian() {
    int size = clusterValues.size();
    int middle = size / 2;

    if (size % 2 != 0) {
      return clusterValues.get(middle);
    } else {
      return (clusterValues.get(middle - 1) + clusterValues.get(middle)) / 2;
    }
  }

  public void addToCluster(Long newClusterItem) {
    clusterValues.add(newClusterItem);
  }

  public Integer getClusterSize() {
    return clusterValues.size();
  }
}
