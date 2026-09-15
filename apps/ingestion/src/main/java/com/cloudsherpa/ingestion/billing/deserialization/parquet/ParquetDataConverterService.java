package com.cloudsherpa.ingestion.billing.deserialization.parquet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.apache.avro.generic.GenericData;
import org.apache.parquet.example.data.simple.NanoTime;
import org.apache.parquet.io.api.Binary;
import org.springframework.stereotype.Service;

@Service
public class ParquetDataConverterService {
  private static final long JULIAN_DAY_OF_UNIX_EPOCH = 2_440_588L;

  public OffsetDateTime getTimestamp(Object value) {

    if (value instanceof GenericData.Fixed fixed) {
      Binary binary = Binary.fromConstantByteArray(fixed.bytes());
      return int96ToOffsetDateTime(binary);
    }

    throw new IllegalArgumentException("The Object is not of type GenricData.Fixed");
  }

  private OffsetDateTime int96ToOffsetDateTime(Binary binary) {
    NanoTime nanoTime = NanoTime.fromBinary(binary);

    long epochDay = nanoTime.getJulianDay() - JULIAN_DAY_OF_UNIX_EPOCH;

    LocalDate date = LocalDate.ofEpochDay(epochDay);
    LocalTime time = LocalTime.ofNanoOfDay(nanoTime.getTimeOfDayNanos());

    return OffsetDateTime.of(date, time, ZoneOffset.UTC);
  }
}
