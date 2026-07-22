package com.cloudsherpa.service.scheduler.dto;

import java.util.List;

public class InstanceScope {
  private String identifierName; // instanceID, ClusterName, DBInstanceIdentifier etc.
  private List<Instance> instances;

  public String getIdentifierName() {
    return identifierName;
  }

  public void setIdentifierName(String name) {
    this.identifierName = name;
  }

  public List<Instance> getInstances() {
    return instances;
  }

  public void setInstances(List<Instance> instances) {
    this.instances = instances;
  }
}
