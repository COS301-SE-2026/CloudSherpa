package com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.deserialization.parquet.ParquetReaderService;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AwsCurParquetNormalizerService {
  private final ParquetReaderService parquetReaderService;

  private final Logger logger = LoggerFactory.getLogger(AwsCurParquetNormalizerService.class);

  AwsCurParquetNormalizerService(ParquetReaderService parquetReaderService) {
    this.parquetReaderService = parquetReaderService;
  }

  public void normalize(Path path) {
    try (ParquetReader<GenericRecord> reader = parquetReaderService.openParquetReader(path)) {
      GenericRecord curRecord;
      while ((curRecord = reader.read()) != null) {
        logger.info("CUR record: {}", curRecord);
      }
    } catch (IOException ioException) {
      logger.error("Failed to initialize parquet reader", ioException);
    }
  }
}
