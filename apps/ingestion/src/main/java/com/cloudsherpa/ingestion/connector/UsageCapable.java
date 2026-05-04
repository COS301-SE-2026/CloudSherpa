package com.cloudsherpa.ingestion.connector;

import com.cloudsherpa.ingestion.models.*;
import java.util.List;

public interface UsageCapable {

  List<UsageRecordModel> fetchUsage(AccountScope scope, IngestionRequestEvent request);
}
