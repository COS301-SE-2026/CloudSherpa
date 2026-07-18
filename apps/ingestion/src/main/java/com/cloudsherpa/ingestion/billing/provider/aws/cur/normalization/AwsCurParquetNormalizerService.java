package com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AwsCurParquetNormalizerService {
  private final ParquetReaderService parquetReaderService;
  private final SherpaDbPersistenceService sherpaDbPersistenceService;
  private final Logger logger = LoggerFactory.getLogger(AwsCurParquetNormalizerService.class);

  private final AwsCurParquetRecordNormalizer recordNormalizer;

  AwsCurParquetNormalizerService(
      ParquetReaderService parquetReaderService,
      SherpaDbPersistenceService sherpaDbPersistenceService) {
    this.parquetReaderService = parquetReaderService;
    this.sherpaDbPersistenceService = sherpaDbPersistenceService;
    this.recordNormalizer = new AwsCurParquetRecordNormalizer();
  }

  public void normalize(Path path, BillingExport export, UUID userId) {
    try (ParquetReader<GenericRecord> reader = parquetReaderService.openParquetReader(path)) {
      GenericRecord curRecord;
      int rowsProcessed = 0;
      while ((curRecord = reader.read()) != null) {
        rowsProcessed++;
        NormalizedCosts normalizedCostsRecord = recordNormalizer.normalize(curRecord, export);
        sherpaDbPersistenceService.recordCost(normalizedCostsRecord, userId);
      }

      export.setRowsProcessed(export.getRowsProcessed() + rowsProcessed);
    } catch (IOException ioException) {
      logger.error("Failed to initialize parquet reader", ioException);
    }
  }
}
