package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.*;
import java.util.List;

public interface BillingCapable {
  List<BillingRecordModel> fetchBilling(AccountScope scope, IngestionRequestEvent request);
}
