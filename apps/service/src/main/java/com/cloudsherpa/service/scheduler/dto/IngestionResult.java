package com.cloudsherpa.service.scheduler.dto;

import java.util.List;

public class IngestionResult {

  private List<UsageRecordModel> usage;
  private List<BillingRecordModel> billing;

  public IngestionResult(List<UsageRecordModel> usage, List<BillingRecordModel> billing) {
    this.usage = usage;
    this.billing = billing;
  }

  public List<UsageRecordModel> getUsage() {
    return this.usage;
  }

  public List<BillingRecordModel> getBilling() {
    return this.billing;
  }
}
