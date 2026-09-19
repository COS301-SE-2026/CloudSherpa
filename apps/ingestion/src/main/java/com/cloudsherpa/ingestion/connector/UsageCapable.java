package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.*;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;

public interface UsageCapable {

  void fetchUsage(AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer);

  void fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer);
}
