package com.cloudsherpa.ingestion.billing.provider.aws.cur.deserialization.parquet;

import java.io.IOException;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.LocalInputFile;
import org.springframework.stereotype.Service;

@Service
public class ParquetReaderService {
  public ParquetReader<GenericRecord> openParquetReader(java.nio.file.Path javaPath)
      throws IOException {
    Configuration configuration = new Configuration();
    // CUR timestamp compatibility
    configuration.setBoolean("parquet.avro.readInt96AsFixed", true);

    InputFile inputFile = new LocalInputFile(javaPath);

    return AvroParquetReader.<GenericRecord>builder(inputFile).withConf(configuration).build();
  }
}
