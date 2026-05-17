-- Enables TimescaleDB, an extension for Postgres optimized for time-series data.
-- It makes querying data over time much faster.
CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TYPE provider_enum AS ENUM ('AWS','AZURE','GCP');
CREATE TYPE status_enum AS ENUM ('active','disabled');
CREATE TYPE credential_type_enum AS ENUM ('access_key','oauth');
CREATE TYPE account_type_enum AS ENUM ('aws_account','azure_subscription','gcp_project');
CREATE TYPE metric_type_enum AS ENUM ('cost','usage','performance');

CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  email VARCHAR(320) NOT NULL UNIQUE,
  username VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE cloud_connection (
  connection_id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(user_id),
  provider provider_enum NOT NULL,
  status status_enum NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE cloud_account (
  account_id UUID PRIMARY KEY,
  connection_id UUID NOT NULL REFERENCES cloud_connection(connection_id),
  account_type account_type_enum NOT NULL,
  display_name VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE resource (
  resource_id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES cloud_account(account_id),
  resource_type VARCHAR(255),
  tags VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE cloud_credential (
  credential_id UUID PRIMARY KEY,
  connection_id UUID NOT NULL REFERENCES cloud_connection(connection_id),
  provider provider_enum NOT NULL,
  credential_type credential_type_enum NOT NULL,
  credential_value TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE normalized_metrics (
  metric_id UUID,
  account_id UUID NOT NULL REFERENCES cloud_account(account_id),
  recorded_at TIMESTAMPTZ NOT NULL,
  resource_id UUID REFERENCES resource(resource_id),
  metric_type metric_type_enum NOT NULL,
  metric_name VARCHAR(255) NOT NULL,
  metric_value NUMERIC NOT NULL,
  unit VARCHAR(50),
  currency VARCHAR(10),
  period_start TIMESTAMPTZ,
  period_end TIMESTAMPTZ,
  PRIMARY KEY (recorded_at, metric_id)
);

-- Converts the standard Postgres table above into a TimescaleDB "hypertable".
-- This partitions the data behind the scenes based on 'recorded_at', 
-- making time-based queries lightning fast as the table grows to millions of rows.
SELECT create_hypertable('normalized_metrics', 'recorded_at');

-- Creates an index to speed up lookups when you filter by a specific environment 
-- and order the results from newest to oldest.
CREATE INDEX ix_resource_time ON normalized_metrics (resource_id, recorded_at DESC);

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