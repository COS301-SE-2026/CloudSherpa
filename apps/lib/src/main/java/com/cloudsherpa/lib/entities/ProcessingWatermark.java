package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_watermark", schema = "public", 
    uniqueConstraints = @UniqueConstraint(name = "uq_processing_watermark_user_pipeline", 
    columnNames = {"user_id", "pipeline_name"}))
public class ProcessingWatermark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "watermark_id", nullable = false, updatable = false)
    private UUID watermarkId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "pipeline_name", nullable = false, length = 255)
    private String pipelineName;

    @Column(name = "last_processed_period")
    private OffsetDateTime lastProcessedPeriod;

    @Column(name = "last_successful_run")
    private OffsetDateTime lastSuccessfulRun;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected ProcessingWatermark() {}

    public ProcessingWatermark(
        UUID userId,
        String pipelineName,
        OffsetDateTime lastProcessedPeriod,
        OffsetDateTime lastSuccessfulRun,
        OffsetDateTime updatedAt) {
        this.userId = userId;
        this.pipelineName = pipelineName;
        this.lastProcessedPeriod = lastProcessedPeriod;
        this.lastSuccessfulRun = lastSuccessfulRun;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public OffsetDateTime getLastProcessedPeriod() {
        return lastProcessedPeriod;
    }

    public OffsetDateTime getLastSuccessfulRun() {
        return lastSuccessfulRun;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setLastProcessedPeriod(OffsetDateTime lastProcessedPeriod) {
        this.lastProcessedPeriod = lastProcessedPeriod;
    }

    public void setLastSuccessfulRun(OffsetDateTime lastSuccessfulRun) {
        this.lastSuccessfulRun = lastSuccessfulRun;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}