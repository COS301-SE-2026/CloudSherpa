package com.cloudsherpa.ingestion.billing.provider.aws.cur.serialization.json;

public class DataFile {
  private String key;
  private long sizeBytes;

  public DataFile() {
    // Required by Jackson
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public void setSizeBytes(long sizeBytes) {
    this.sizeBytes = sizeBytes;
  }
}
