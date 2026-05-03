-- Enables TimescaleDB, an extension for Postgres optimized for time-series data.
-- It makes querying data over time much faster.
CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE environment_reference (
    environment_id UUID PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Where the Ingestion service saves normalized data.
CREATE TABLE normalized_metrics (
    metric_id UUID NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    environment_id UUID REFERENCES environment_reference(environment_id),
    resource_id VARCHAR(255) NOT NULL,
    service_category VARCHAR(100) NOT NULL,
    usage_amount NUMERIC NOT NULL,
    usage_unit VARCHAR(50) NOT NULL,
    cost_amount NUMERIC NOT NULL,
    currency VARCHAR(10) DEFAULT 'ZAR'
);

-- Mock environment_reference for mock testing
INSERT INTO environment_reference (environment_id, provider, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'AWS', NOW())
ON CONFLICT (environment_id) DO NOTHING;

-- Converts the standard Postgres table above into a TimescaleDB "hypertable".
-- This partitions the data behind the scenes based on 'recorded_at', 
-- making time-based queries lightning fast as the table grows to millions of rows.
SELECT create_hypertable('normalized_metrics', 'recorded_at');

-- Creates an index to speed up lookups when you filter by a specific environment 
-- and order the results from newest to oldest.
CREATE INDEX ix_environment_time ON normalized_metrics (environment_id, recorded_at DESC);

-- Defines the custom function that will do the broadcasting.
-- "RETURNS TRIGGER" restricts this function so it can only be executed by a table trigger. (Like INSERT)
CREATE OR REPLACE FUNCTION notify_metric_event() RETURNS TRIGGER AS $$
BEGIN
  PERFORM pg_notify('metric_events', row_to_json(NEW)::text); 
  -- 'PERFORM' is used instead of 'SELECT' because we are executing a command 
  -- but we don't care about getting a result back.

  -- 'metric_events' is the channel we are broadcasting on.
  -- 'NEW' is a built-in variable holding the exact row data that triggered this function.
  -- row_to_json() packages that row into a clean JSON object format.
  -- ::text converts that JSON object into a plain string, because pg_notify ONLY accepts strings.

  -- it hands the data back to Postgres to finish the insert.
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- "AFTER INSERT" guarantees we only broadcast IF the data was successfully saved to disk.
-- "FOR EACH ROW" ensures that if 100 rows are inserted in a single query, 
-- the function runs 100 times, sending 100 separate JSON payloads.
CREATE TRIGGER metric_notify_trigger
AFTER INSERT ON normalized_metrics
FOR EACH ROW EXECUTE FUNCTION notify_metric_event();