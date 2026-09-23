package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class IngestionPersistenceService {
  private final SherpaDbPersistenceService sherpaDbPersistenceService;

  Logger logger = Logger.getLogger(getClass().getName());

  public IngestionPersistenceService(SherpaDbPersistenceService sherpaDbPersistenceService) {
    this.sherpaDbPersistenceService = sherpaDbPersistenceService;
  }

  public void normalizeAndPersistUsage(
      List<UsageRecordModel> usageRecords, UUID userId, boolean isBackfill, Normalizer normalizer) {
    if (usageRecords == null || usageRecords.isEmpty()) {
      return;
    }

    for (UsageRecordModel r : usageRecords) {
      NormalizedMetric normalized = normalizer.normalize(r);
      if (normalized != null) {
        try {
          writeToSherpaDb(normalized, r, userId, isBackfill);
        } catch (RuntimeException ex) {
          logger.warning("Failed to persist normalized metric: " + ex.getMessage());
        }
      }
    }
  }

  private void writeToSherpaDb(
      NormalizedMetric metric, UsageRecordModel r, UUID userId, boolean isBackfill) {
    sherpaDbPersistenceService.recordMetric(metric, r, userId, isBackfill);
  }
}
