package com.cloudsherpa.ingestion.models;

import java.time.Instant;
import java.util.UUID;

public interface Record {

  UUID getRecordId();

  void setRecordId(UUID recordId);

  String getProvider();

  void setProvider(String provider);

  Instant getIngestionTimestamp();

  void setIngestionTimestamp(Instant ingestionTimestamp);

  String getIngestionId();

  void setIngestionId(String ingestionId);

  String getSource();

  void setSource(String source);

}
