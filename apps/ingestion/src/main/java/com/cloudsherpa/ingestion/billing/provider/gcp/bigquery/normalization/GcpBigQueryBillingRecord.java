package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.google.cloud.bigquery.FieldValueList;

public record GcpBigQueryBillingRecord(
    FieldValueList fieldValueList, CreditProcessingState creditProcessingState) {}
