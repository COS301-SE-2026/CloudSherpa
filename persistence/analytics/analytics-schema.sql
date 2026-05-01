CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE normalized_metrics (
    metric_id UUID NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    environment_id UUID NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    service_category VARCHAR(100) NOT NULL,
    usage_amount NUMERIC NOT NULL,
    usage_unit VARCHAR(50) NOT NULL,
    cost_amount NUMERIC NOT NULL,
    currency VARCHAR(10) DEFAULT 'ZAR'
);

SELECT create_hypertable('normalized_metrics', 'recorded_at');

CREATE INDEX ix_environment_time ON normalized_metrics (environment_id, recorded_at DESC);