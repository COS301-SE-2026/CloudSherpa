package com.cloudsherpa.ingestion.billing.provider.aws.cur;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.discovery.AwsCurReportDiscovery;
import org.springframework.stereotype.Service;

@Service
public class AwsCurIngestionService {
  private final AwsCurConfig config;
  private final AwsCurReportDiscovery awsCurReportDiscovery;

  public AwsCurIngestionService(AwsCurReportDiscovery awsCurReportDiscovery) {
    this.config =
        new AwsCurConfig("test-bucket-564907680089-eu-north-1-an", "/exports", "CloudSherpaExport");
    this.awsCurReportDiscovery = awsCurReportDiscovery;
  }

  public void runCurIngestion() {
    awsCurReportDiscovery.discoverCurReports(config);
  }
}
