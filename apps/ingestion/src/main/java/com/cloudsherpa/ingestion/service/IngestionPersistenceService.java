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
      List<UsageRecordModel> usageRecords, UUID userId, Normalizer normalizer) {
    if (usageRecords == null || usageRecords.isEmpty()) {
      return;
    }

    for (UsageRecordModel r : usageRecords) {
      NormalizedMetric normalized = normalizer.normalize(r);
      if (normalized != null) {
        try {
          writeToSherpaDb(normalized, r, userId);
        } catch (RuntimeException ex) {
          logger.warning("Failed to persist normalized metric: " + ex.getMessage());
        }
      }
    }
  }

  private void writeToSherpaDb(NormalizedMetric metric, UsageRecordModel r, UUID userId) {
    sherpaDbPersistenceService.recordMetric(metric, r, userId);
  }
}
