package com.cloudsherpa.lib.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "gcp_billing_export_config", schema = "public")
public class GcpBillingExportConfig {
    @Id
    @Column(name = "config_id", nullable = false)
    private UUID configId;

    @OneToOne
    @JoinColumn(name = "config_id", nullable = false, insertable = false, updatable = false)
    private BillingExportConfig config;

    @Column(name = "dataset_id", length = 1024, nullable = false)
    private String datasetId;

    @Column(name = "billing_account_id", length = 20, nullable = false)
    private String billingAccountId;

    protected GcpBillingExportConfig() {}

    public GcpBillingExportConfig(UUID configId, String datasetId, String billingAccountId) {
        this.configId = configId;
        this.datasetId = datasetId;
        this.billingAccountId = billingAccountId;
    }

    public UUID getConfigId() {
        return configId;
    }

    public BillingExportConfig getConfig() {
        return config;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getBillingAccountId() {
        return billingAccountId;
    }
}
