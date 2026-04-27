-- AnalyticsDB init script
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Provides a valid foreign key target for the normalized_metrics table.
-- Enables cross-cloud cost comparison by storing the 'provider' (AWS/GCP/AZURE).
CREATE TABLE environment_reference (
    environment_id UUID PRIMARY KEY,
    provider VARCHAR(50) NOT NULL, -- 'AWS', 'GCP', or 'AZURE'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- This stores the output of the Normalizer service
CREATE TABLE normalized_metrics (
    recorded_at TIMESTAMPTZ NOT NULL,
    environment_id UUID REFERENCES environment_reference(environment_id),
    resource_id VARCHAR(255) NOT NULL,
    service_category VARCHAR(100) NOT NULL,
    usage_amount NUMERIC NOT NULL,
    usage_unit VARCHAR(50) NOT NULL,
    cost_amount NUMERIC NOT NULL,
    currency VARCHAR(10) DEFAULT 'ZAR'
);

-- Turn the standard Postgres table into a TimescaleDB Hypertable partitioned by time
-- This chunks the data under the hood for massive performance gains
SELECT create_hypertable('normalized_metrics', 'recorded_at');

-- Create an index to make querying by environment and resource lightning fast
-- When a user opens their dashboard, the first thing the system does is filter for their specific cloud accounts 
-- and the most recent costs ordered in DESC order (newest timestamps at the top of the index).
CREATE INDEX ix_environment_time ON normalized_metrics (environment_id, recorded_at DESC);