package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import java.io.IOException;
import java.util.List;

public interface ExportReader<T> extends AutoCloseable {

  List<T> readBatch(int maxRows) throws IOException;

  @Override
  void close() throws IOException;
}
