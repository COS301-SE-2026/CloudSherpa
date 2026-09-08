package com.cloudsherpa.lib.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "azure_billing_export_config", schema = "public")
public class AzureBillingExportConfig {
    @Id 
    @Column(name = "config_id", nullable = false)
    UUID configId;

    @OneToOne
    @JoinColumn(name = "config_id", nullable = false, updatable = false, insertable = false)
    private BillingExportConfig config;

    @Column(name = "storage_account_name", length = 24, nullable = false)
    private String storageAccountName;

    @Column(name = "storage_container", length = 63, nullable = false)
    private String storageContainer;

    @Column(name = "billing_export_directory", length = 255, nullable = false)
    private String billingExportDirectory;

    @Column(name = "export_name", nullable = false)
    private String billingExportName;

    protected AzureBillingExportConfig() {}

    public AzureBillingExportConfig(
        UUID configId,
        String storageAccountName,
        String storageContainer,
        String billingExportDirectory,
        String billingExportName
    ) {
        this.configId = configId;
        this.storageAccountName = storageAccountName;
        this.storageContainer = storageContainer;
        this.billingExportDirectory = billingExportDirectory;
        this.billingExportName = billingExportName;
    }

    public UUID getConfigId() {
        return configId;
    }

    public void setConfigId(UUID configId) {
        this.configId = configId;
    }

    public BillingExportConfig getConfig() {
        return config;
    }

    public void setConfig(BillingExportConfig config) {
        this.config = config;
    }

    public String getStorageAccountName() {
        return storageAccountName;
    }

    public void setStorageAccountName(String storageAccountName) {
        this.storageAccountName = storageAccountName;
    }

    public String getStorageContainer() {
        return storageContainer;
    }

    public void setStorageContainer(String storageContainer) {
        this.storageContainer = storageContainer;
    }

    public String getBillingExportDirectory() {
        return billingExportDirectory;
    }

    public void setBillingExportDirectory(String billingExportDirectory) {
        this.billingExportDirectory = billingExportDirectory;
    }

    public String getBillingExportName() {
        return billingExportName;
    }

    public void setBillingExportName(String billingExportName) {
        this.billingExportName = billingExportName;
    }
}
