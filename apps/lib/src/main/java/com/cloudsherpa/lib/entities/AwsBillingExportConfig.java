package com.cloudsherpa.lib.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "aws_billing_export_config", schema = "public")
public class AwsBillingExportConfig {
    @Id
    @Column(name = "config_id", nullable = false)
    private UUID configId;

    @OneToOne
    @JoinColumn(name = "config_id", nullable = false, updatable = false, insertable = false)
    private BillingExportConfig config;

    @Column(name = "bucket_name", nullable = false, length = 63)
    private String bucketName;

    @Column(name = "bucket_region", nullable = false, length = 50)
    private String bucketRegion;

    @Column(name = "export_prefix", length = 256)
    private String exportPrefix;

    @Column(name = "export_name", nullable = false, length = 128)
    private String exportName;

    protected AwsBillingExportConfig() {}
}
