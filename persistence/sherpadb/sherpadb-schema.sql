-- Create the shared global schema
-- This holds data that spans across all users (users, dashboards, credentials).
CREATE SCHEMA IF NOT EXISTS public;

CREATE TYPE public.provider_enum AS ENUM ('AWS', 'AZURE', 'GCP');
CREATE TYPE public.status_enum AS ENUM ('active', 'disabled');
CREATE TYPE public.credential_type_enum AS ENUM ('access_key', 'oauth');
CREATE TYPE public.account_type_enum AS ENUM ('aws_account', 'azure_subscription', 'gcp_project');
CREATE TYPE public.metric_type_enum AS ENUM ('cost', 'usage', 'performance');
CREATE TYPE public.theme_enum AS ENUM ('light', 'dark');
CREATE TYPE public.currency_enum AS ENUM ('USD', 'EUR', 'ZAR');
CREATE TYPE public.language_enum AS ENUM ('en', 'es', 'fr');
CREATE TYPE public.ingestion_period_enum AS ENUM ('1m', '5m', '1h');
CREATE TYPE public.predefined_time_enum AS ENUM ('last_1h', 'last_24h', 'last_7d');
CREATE TYPE public.type_enum AS ENUM ('line_chart', 'guage_chart');

-- Global Tables in Public Schema
-- These tables use ON DELETE CASCADE so if a user deletes their account, 
-- all their preferences, connections, and dashboards are automatically cleaned up.
CREATE TABLE public.users (
  user_id uuid PRIMARY KEY,
  email varchar(320) UNIQUE NOT NULL,
  username varchar(100) NOT NULL,
  password_hash varchar(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE public.preferences (
  user_id uuid PRIMARY KEY REFERENCES public.users(user_id) ON DELETE CASCADE,
  theme public.theme_enum,
  background text, 
  currency public.currency_enum,
  language public.language_enum,
  sidebar_toggle boolean DEFAULT true
);

CREATE TABLE public.cloud_connection (
  connection_id uuid PRIMARY KEY,
  user_id uuid REFERENCES public.users(user_id) ON DELETE CASCADE,
  provider public.provider_enum NOT NULL,
  status public.status_enum DEFAULT 'active',
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE public.cloud_credential (
  credential_id uuid PRIMARY KEY,
  connection_id uuid REFERENCES public.cloud_connection(connection_id) ON DELETE CASCADE,
  provider public.provider_enum NOT NULL,
  credential_type public.credential_type_enum NOT NULL,
  credential_value text NOT NULL,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE public.cloud_account (
  account_id uuid PRIMARY KEY,
  connection_id uuid REFERENCES public.cloud_connection(connection_id) ON DELETE CASCADE,
  account_type public.account_type_enum NOT NULL,
  ingestion_period public.ingestion_period_enum,
  display_name varchar(255),
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE public.dashboard (
  dashboard_id uuid PRIMARY KEY,
  user_id uuid REFERENCES public.users(user_id) ON DELETE CASCADE,
  time_from timestamptz,
  time_to timestamptz,
  predefined_time public.predefined_time_enum
);

CREATE TABLE public.widget (
  widget_id uuid PRIMARY KEY,
  dashboard_id uuid REFERENCES public.dashboard(dashboard_id) ON DELETE CASCADE,
  type public.type_enum NOT NULL,
  start_x integer NOT NULL, 
  start_y integer NOT NULL, 
  width integer NOT NULL,   
  height integer NOT NULL,  
  display_name varchar(100)
);

CREATE TABLE public.widget_resource (
  widget_resource_id uuid PRIMARY KEY,
  widget_id uuid REFERENCES public.widget(widget_id) ON DELETE CASCADE,
  resource_id uuid NOT NULL, 
  metric_type public.metric_type_enum NOT NULL
);

-- This sits in the public schema so it only has to be written once, but it is 
-- smart enough to broadcast on a specific tenant's channel dynamically.
CREATE OR REPLACE FUNCTION public.notify_metric_event() 
RETURNS TRIGGER AS $$
BEGIN
    -- TG_TABLE_SCHEMA dynamically grabs the name of the schema that fired the trigger.
    -- Example: If a metric hits tenant_1234, it broadcasts on 'metric_events_tenant_1234'.
    -- row_to_json(NEW) turns the newly inserted row into a JSON object.
    PERFORM pg_notify('metric_events_' || TG_TABLE_SCHEMA, row_to_json(NEW)::text); 
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Backend calls `SELECT public.create_new_tenant('uuid');` to run this.
-- It dynamically generates a new schema for the new user.
CREATE OR REPLACE FUNCTION public.create_new_tenant(new_tenant_id UUID)
RETURNS void AS $$
DECLARE
    -- Formats the UUID into a specific shema name for the user (e.g., 'tenant_123e4567...')
    schema_name TEXT := 'tenant_' || replace(new_tenant_id::text, '-', '_');
BEGIN
    -- Create the isolated schema for the tenant
    -- We use %I in the format string. This safely quotes the schema name to prevent SQL Injection.
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I;', schema_name);

    -- Build the resource table
    EXECUTE format($sql$
        CREATE TABLE %I.resource (
            resource_id uuid PRIMARY KEY,
            account_id uuid REFERENCES public.cloud_account(account_id) ON DELETE CASCADE, 
            resource_type varchar(255) NOT NULL,
            resource_name varchar(255) NOT NULL,
            status public.status_enum,
            tags jsonb,
            last_updated timestamptz DEFAULT NOW(),
            created_at timestamptz DEFAULT NOW()
        );
    $sql$, schema_name);

    -- The GIN Index for JSONB Tags
    -- %1$I means "use the first variable (schema_name) and format it safely as an Identifier".
    EXECUTE format($sql$
        CREATE INDEX ix_%1$s_resource_tags ON %1$I.resource USING GIN (tags);
    $sql$, schema_name);

    -- Build the metrics table
    EXECUTE format($sql$
        CREATE TABLE %I.normalized_metrics (
            metric_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
            resource_id uuid REFERENCES %I.resource(resource_id) ON DELETE CASCADE,
            recorded_at timestamptz NOT NULL,
            metric_type public.metric_type_enum NOT NULL,
            metric_name varchar(255) NOT NULL,
            metric_value numeric NOT NULL,
            unit varchar(50),
            currency varchar(10),
            period_start timestamptz NOT NULL,
            period_end timestamptz NOT NULL
        );
    $sql$, schema_name, schema_name);

    PERFORM create_hypertable(
        format('%I.normalized_metrics', schema_name), 
        'period_start'
    );

    EXECUTE format($sql$
        CREATE INDEX ix_%1$s_resource_metric_time 
        ON %1$I.normalized_metrics (resource_id, metric_name, period_start DESC);
    $sql$, schema_name);

    -- Attach the Real-Time Broadcast Trigger
    -- Attach the trigger specifically to this new user's metrics table, 
    -- but tell it to execute the shared global function we defined in the public shema.
    EXECUTE format($sql$
        CREATE TRIGGER metric_notify_trigger
        AFTER INSERT ON %1$I.normalized_metrics
        FOR EACH ROW EXECUTE FUNCTION public.notify_metric_event();
    $sql$, schema_name);

END;
$$ LANGUAGE plpgsql;