package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.*;
import java.util.List;

public interface UsageCapable {

  List<UsageRecordModel> fetchUsage(AccountScope accountScope, IngestionRequestEvent request);

  List<UsageRecordModel> fetchMockUsage(AccountScope accountScope, IngestionRequestEvent request);
}
