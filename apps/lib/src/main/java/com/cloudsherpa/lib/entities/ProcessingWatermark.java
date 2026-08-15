package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "processing_watermark")
public class ProcessingWatermark {

    @Id
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
        String pipelineName,
        OffsetDateTime lastProcessedPeriod,
        OffsetDateTime lastSuccessfulRun,
        OffsetDateTime updatedAt) {
        this.pipelineName = pipelineName;
        this.lastProcessedPeriod = lastProcessedPeriod;
        this.lastSuccessfulRun = lastSuccessfulRun;
        this.updatedAt = updatedAt;
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