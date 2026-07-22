package com.cloudsherpa.service.scheduler.dto;

public class Instance {
  private String identifier; // e.g. i-12345
  private String region; // e.g. af-south-1 if applicable

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }
}
