package com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.apache.avro.generic.GenericData.Fixed;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.example.data.simple.NanoTime;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.api.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AwsCurParquetNormalizerService {
  private final ParquetReaderService parquetReaderService;
  private final SherpaDbPersistenceService sherpaDbPersistenceService;
  private final Logger logger = LoggerFactory.getLogger(AwsCurParquetNormalizerService.class);
  private static final long JULIAN_DAY_OF_UNIX_EPOCH = 2_440_588L;

  AwsCurParquetNormalizerService(
      ParquetReaderService parquetReaderService,
      SherpaDbPersistenceService sherpaDbPersistenceService) {
    this.parquetReaderService = parquetReaderService;
    this.sherpaDbPersistenceService = sherpaDbPersistenceService;
  }

  public void normalize(Path path, BillingExport export, UUID userId) {
    try (ParquetReader<GenericRecord> reader = parquetReaderService.openParquetReader(path)) {
      GenericRecord curRecord;
      int rowsProcessed = 0;
      while ((curRecord = reader.read()) != null) {
        rowsProcessed++;

        NormalizedCosts normalizedCostsRecord = new NormalizedCosts();

        normalizedCostsRecord.setExecutionId(export.getUuidExportId());
        normalizedCostsRecord.setProvider(ProviderEnum.AWS);
        normalizedCostsRecord.setBillingAccountId(
            curRecord.get("line_item_usage_account_id").toString());

        if (curRecord.get("line_item_line_item_type").toString().equals("Usage")) {
          normalizedCostsRecord.setChargeType(ChargeTypeEnum.Usage);
        } else {
          normalizedCostsRecord.setChargeType(ChargeTypeEnum.Other);
        }

        Object serviceCode = curRecord.get("product_servicecode");
        if (serviceCode == null) {
          logger.info("Required field product_servicecode code is not present, skipping record...");
          continue;
        }
        normalizedCostsRecord.setServiceName(curRecord.get("product_servicecode").toString());

        Object costAmount = curRecord.get("line_item_unblended_cost");
        if (costAmount instanceof Number number) {
          normalizedCostsRecord.setCostAmount(BigDecimal.valueOf(number.doubleValue()));
        } else {
          logger.error("Required field line_item_unblended_cost not present, skipping record...");
          continue;
        }

        logger.info(
            "Timestamp type '{}'",
            curRecord.get("line_item_usage_start_date").getClass().getName());

        Object startDate = curRecord.get("line_item_usage_start_date");

        if (startDate instanceof Fixed fixedStartDate) {
          Binary binary = Binary.fromConstantByteArray(fixedStartDate.bytes());
          normalizedCostsRecord.setUsageStartTime(int96ToOffsetDateTime(binary));
        } else {
          logger.error("Failed convert timestamp");
          continue;
        }

        Object endDate = curRecord.get("line_item_usage_end_date");

        if (endDate instanceof Fixed fixedEndDate) {
          Binary binary = Binary.fromConstantByteArray(fixedEndDate.bytes());
          normalizedCostsRecord.setUsageEndTime(int96ToOffsetDateTime(binary));
        } else {
          logger.error("Failed convert timestamp");
          continue;
        }

        sherpaDbPersistenceService.recordCost(normalizedCostsRecord, userId);
      }

      export.setRowsProcessed(export.getRowsProcessed() + rowsProcessed);
    } catch (IOException ioException) {
      logger.error("Failed to initialize parquet reader", ioException);
    }
  }

  private OffsetDateTime int96ToOffsetDateTime(Binary binary) {
    NanoTime nanoTime = NanoTime.fromBinary(binary);

    long epochDay = nanoTime.getJulianDay() - JULIAN_DAY_OF_UNIX_EPOCH;

    LocalDate date = LocalDate.ofEpochDay(epochDay);
    LocalTime time = LocalTime.ofNanoOfDay(nanoTime.getTimeOfDayNanos());

    return OffsetDateTime.of(date, time, ZoneOffset.UTC);
  }
}
