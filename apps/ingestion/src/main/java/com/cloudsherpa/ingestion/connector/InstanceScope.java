package com.cloudsherpa.ingestion.connector;

import java.util.List;

public class InstanceScope {
  private String identifierName; // instanceID, ClusterName, DBInstanceIdentifier etc.
  private List<String> values; // i-21xxxxx, i-35xxxxx etc.

  public String getIdentifierName() {
    return identifierName;
  }

  public void setIdentifierName(String name) {
    this.identifierName = name;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }
}
