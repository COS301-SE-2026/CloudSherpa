package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.*;
import java.util.List;

public interface BillingCapable {
  List<BillingRecordModel> fetchBilling(AccountScope accountScope, IngestionRequestEvent request);

  List<BillingRecordModel> fetchMockBilling(
      AccountScope accountScope, IngestionRequestEvent request);
}
