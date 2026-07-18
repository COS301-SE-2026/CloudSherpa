package com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContext;
import com.cloudsherpa.ingestion.provider.aws.services.s3.S3ObjectUriReference;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
public class AwsCurCsvNormalizerService {

  Logger logger = LoggerFactory.getLogger(AwsCurCsvNormalizerService.class);

  private final SherpaDbPersistenceService sherpaDbPersistenceService;

  public AwsCurCsvNormalizerService(SherpaDbPersistenceService sherpaDbPersistenceService) {
    this.sherpaDbPersistenceService = sherpaDbPersistenceService;
  }

  public void normalize(String objectUri, AwsCurContext context, BillingExport export) {
    try (S3Client s3 = S3Client.builder().region(Region.EU_NORTH_1).build()) {

      S3ObjectUriReference s3Uri = context.getS3().uriHelper(s3, objectUri);

      GetObjectRequest request =
          GetObjectRequest.builder().bucket(s3Uri.bucketName()).key(s3Uri.key()).build();

      normalizeFromCsv(s3, request, export);
    }
  }

  private void normalizeFromCsv(S3Client s3, GetObjectRequest request, BillingExport export) {
    try (ResponseInputStream<GetObjectResponse> s3Stream = s3.getObject(request);
        GZIPInputStream gzipStream = new GZIPInputStream(s3Stream);
        Reader reader =
            new BufferedReader(new InputStreamReader(gzipStream, StandardCharsets.UTF_8));
        CSVParser parser =
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader)) {
      int rowsProcessed = 0;
      for (CSVRecord csvRecord : parser) {
        rowsProcessed++;
        logger.info("{}", csvRecord);
      }

      export.setRowsProcessed(export.getRowsProcessed() + rowsProcessed);
    } catch (IOException exception) {
      throw new RuntimeException("Failed to open CSV Parser ", exception);
    }
  }
}
