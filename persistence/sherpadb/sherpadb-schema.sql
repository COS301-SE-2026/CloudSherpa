-- ----------------------------------------------------------------
-- GLOBAL ENUMS & EXTENSIONS
-- ----------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS public;

CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

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
CREATE TYPE public.type_enum AS ENUM ('KPI', 'CHART');
CREATE TYPE public.execution_status_enum AS ENUM ('pending', 'processing', 'completed', 'failed');
CREATE TYPE PUBLIC.chart_type_enum AS ENUM ('gauge_chart', 'line_chart');
-- Differentiates actual compute usage from other types.
-- Maps to CUR: line_item_line_item_type
CREATE TYPE public.charge_type_enum AS ENUM ('Usage', 'Other'); 

-- ----------------------------------------------------------------
-- PUBLIC TABLES 
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.users (
  user_id uuid PRIMARY KEY,
  email varchar(320) UNIQUE NOT NULL,
  username varchar(100) NOT NULL,
  password_hash varchar(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.preferences (
  user_id uuid PRIMARY KEY REFERENCES public.users(user_id) ON DELETE CASCADE,
  theme public.theme_enum,
  background text, 
  currency public.currency_enum,
  language public.language_enum,
  sidebar_toggle boolean DEFAULT true
);

CREATE TABLE IF NOT EXISTS public.cloud_connection (
  connection_id uuid PRIMARY KEY,
  user_id uuid REFERENCES public.users(user_id) ON DELETE CASCADE,
  provider public.provider_enum NOT NULL,
  status public.status_enum DEFAULT 'active',
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.cloud_account (
  account_id uuid PRIMARY KEY,
  connection_id uuid REFERENCES public.cloud_connection(connection_id) ON DELETE CASCADE,
  account_type public.account_type_enum NOT NULL,
  ingestion_period public.ingestion_period_enum,
  display_name varchar(255),
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.cloud_credential (
  credential_id uuid PRIMARY KEY,
  account_id uuid UNIQUE REFERENCES public.cloud_account(account_id) ON DELETE CASCADE,
  provider public.provider_enum NOT NULL,
  credential_type public.credential_type_enum NOT NULL,
  credential_value text NOT NULL,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.billing_export_config (
  config_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  account_id uuid REFERENCES public.cloud_account(account_id) ON DELETE CASCADE,
  bucket_name varchar(255) NOT NULL,
  export_prefix varchar(255), 
  export_name varchar(255) NOT NULL,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.billing_export_execution (
  execution_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  config_id uuid REFERENCES public.billing_export_config(config_id) ON DELETE CASCADE,
  status public.execution_status_enum DEFAULT 'pending',
  rows_processed integer DEFAULT 0,
  started_at timestamptz DEFAULT NOW(),
  completed_at timestamptz,
  error_message text
);

CREATE TABLE IF NOT EXISTS public.dashboard (
  dashboard_id uuid PRIMARY KEY,
  display_name varchar(255) NOT NULL,
  user_id uuid REFERENCES public.users(user_id) ON DELETE CASCADE,
  time_from timestamptz,
  time_to timestamptz,
  predefined_time public.predefined_time_enum,
  current boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS public.widget (
  widget_id uuid PRIMARY KEY,
  dashboard_id uuid REFERENCES public.dashboard(dashboard_id) ON DELETE CASCADE,
  type public.type_enum NOT NULL,
  start_x integer NOT NULL, 
  start_y integer NOT NULL, 
  width integer NOT NULL,   
  height integer NOT NULL,  
  display_name varchar(100)
);

CREATE TABLE IF NOT EXISTS public.widget_kpi (
  kpi_id uuid PRIMARY KEY,
  widget_id uuid REFERENCES public.widget(widget_id) ON DELETE CASCADE,
  aggregation integer NOT NULL
);

CREATE TABLE IF NOT EXISTS public.kpi_charges (
  kpi_charges_id uuid PRIMARY KEY,
  widget_kpi_id uuid REFERENCES public.widget_kpi(kpi_id) NOT NULL,
  charge_id varchar (2128) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.widget_chart (
  chart_id uuid PRIMARY KEY,
  widget_id uuid REFERENCES public.widget(widget_id) ON DELETE CASCADE,
  chart_type public.chart_type_enum NOT NULL
);

CREATE TABLE IF NOT EXISTS public.chart_resource (
  chart_resource_id uuid PRIMARY KEY,
  widget_chart_id uuid REFERENCES public.widget_chart(chart_id) ON DELETE CASCADE,
  resource_id uuid NOT NULL, 
  metric_type public.metric_type_enum NOT NULL
);

-- ----------------------------------------------------------------
-- GLOBAL FUNCTIONS
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.notify_metric_event() 
RETURNS TRIGGER AS $$
BEGIN
    -- TG_TABLE_SCHEMA dynamically grabs the name of the schema that fired the trigger.
    -- Example: If a metric hits tenant_1234, it broadcasts on 'metric_events_tenant_1234'.
    -- row_to_json(NEW) turns the newly inserted row into a JSON object.
    PERFORM pg_notify('metric_events', row_to_json(NEW)::text);
    PERFORM pg_notify('metric_events_' || TG_TABLE_SCHEMA, row_to_json(NEW)::text);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------
-- TENANT LOGIC
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.create_new_tenant(new_tenant_id UUID)
RETURNS void AS $$
DECLARE
    -- Formats the UUID into a specific shema name for the user (e.g., 'tenant_123e4567...')
    schema_name TEXT := 'tenant_' || replace(new_tenant_id::text, '-', '_');
BEGIN
    -- Create the isolated schema for the tenant
    -- We use %I in the format string. This safely quotes the schema name to prevent SQL Injection.
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I;', schema_name);

    -- --------------------------------------------------------------------------
    -- Tenant Resources
    -- --------------------------------------------------------------------------
    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.resource (
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
        CREATE INDEX IF NOT EXISTS ix_%1$s_resource_tags ON %1$I.resource USING GIN (tags);
    $sql$, schema_name);

    -- --------------------------------------------------------------------------
    -- Tenant Metrics (Hypertable)
    -- --------------------------------------------------------------------------
    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.normalized_metrics (
            metric_id uuid DEFAULT gen_random_uuid(),
            resource_id uuid REFERENCES %I.resource(resource_id) ON DELETE CASCADE,
            recorded_at timestamptz NOT NULL,
            metric_type public.metric_type_enum NOT NULL,
            metric_name varchar(255) NOT NULL,
            metric_value numeric NOT NULL,
            unit varchar(50),
            currency varchar(10),
            period_start timestamptz NOT NULL,
            period_end timestamptz NOT NULL,
            PRIMARY KEY (metric_id, period_start)
        );
    $sql$, schema_name, schema_name);

    -- Create hypertable only if the table is not already a hypertable
    IF NOT EXISTS (
        SELECT 1 FROM timescaledb_information.hypertables 
        WHERE hypertable_schema = schema_name AND hypertable_name = 'normalized_metrics'
    ) THEN
        PERFORM create_hypertable(
            format('%I.normalized_metrics', schema_name), 
            'period_start'
        );
    END IF;

    EXECUTE format($sql$
        CREATE INDEX IF NOT EXISTS ix_%1$s_resource_metric_time 
        ON %1$I.normalized_metrics (resource_id, metric_name, period_start DESC);
    $sql$, schema_name);

    -- Attach the Real-Time Broadcast Trigger
    -- Attach the trigger specifically to this new user's metrics table, 
    -- but tell it to execute the shared global function we defined in the public shema.
    EXECUTE format($sql$
        CREATE OR REPLACE TRIGGER metric_notify_trigger
        AFTER INSERT ON %1$I.normalized_metrics
        FOR EACH ROW EXECUTE FUNCTION public.notify_metric_event();
    $sql$, schema_name);

    -- --------------------------------------------------------------------------
    -- Tenant Costs (Hypertable)
    -- --------------------------------------------------------------------------
    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.normalized_costs (
            cost_id uuid DEFAULT gen_random_uuid(),
            
            -- Traces this specific cost row back to the execution that ingested it.
            execution_id uuid REFERENCES public.billing_export_execution(execution_id) ON DELETE CASCADE,
            
            -- Must be nullable because some costs are not tied to a specific resource.
            resource_id varchar(2048),

            charge_id varchar(2128),

            provider public.provider_enum NOT NULL,

            -- Maps to CUR: line_item_usage_account_id
            billing_account_id varchar(255) NOT NULL, 
            
            -- (e.g., 'AmazonEC2').
            -- Maps to CUR: product_servicecode
            service_name varchar(255) NOT NULL, 
            
            -- Differentiates usage and other types.
            -- Maps to CUR: line_item_line_item_type
            charge_type public.charge_type_enum NOT NULL,
            
            -- Maps to CUR: line_item_unblended_cost
            cost_amount numeric(16, 8) NOT NULL, 
            
            -- Default is USD from AWS
            -- Maps to CUR: line_item_currency_code
            currency public.currency_enum DEFAULT 'USD',
            
            -- Maps to CUR: line_item_usage_start_date
            usage_start_time timestamptz NOT NULL, 
            
            -- Determines the exact time window the charge covers (hourly/daily/monthly).
            -- Maps to CUR: line_item_usage_end_date
            usage_end_time timestamptz NOT NULL,   
        
            metadata jsonb DEFAULT '{}'::jsonb,
      
            PRIMARY KEY (cost_id, usage_start_time)
        );
    $sql$, schema_name, schema_name);

    IF NOT EXISTS (
        SELECT 1 FROM timescaledb_information.hypertables 
        WHERE hypertable_schema = schema_name AND hypertable_name = 'normalized_costs'
    ) THEN
        PERFORM create_hypertable(
            format('%I.normalized_costs', schema_name), 
            'usage_start_time'
        );
    END IF;

    -- Indices to accelerate the most common KPI queries: 
    -- "Cost per resource over time" and "Cost per service over time"
    EXECUTE format($sql$
        CREATE INDEX IF NOT EXISTS ix_%1$s_costs_resource_time 
        ON %1$I.normalized_costs (resource_id, usage_start_time DESC);
    $sql$, schema_name);

    EXECUTE format($sql$
        CREATE INDEX IF NOT EXISTS ix_%1$s_costs_service_time 
        ON %1$I.normalized_costs (service_name, usage_start_time DESC);
    $sql$, schema_name);

END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------
-- DEMO SEED DATA
-- ----------------------------------------------------------------
DO $$
DECLARE
  demo_user_id uuid := '5ebe4340-c5ec-4833-ad93-06abf4609f03';
  demo_connection_id uuid := 'c0000000-0000-0000-0000-000000000001';
  demo_account_id uuid := 'a0000000-0000-0000-0000-000000000001';
  demo_config_id uuid := 'e0000000-0000-0000-0000-000000000001';
  demo_execution_id uuid := 'f0000000-0000-0000-0000-000000000001';
  demo_resource_id_1 uuid := 'b0000000-0000-0000-0000-000000000001';
  demo_resource_id_2 uuid := 'b0000000-0000-0000-0000-000000000002';
  demo_provider public.provider_enum := 'AWS';
  demo_status public.status_enum := 'active';
  demo_account_type public.account_type_enum := 'aws_account';
  demo_ingestion_period public.ingestion_period_enum := '1h';
  demo_charge_type public.charge_type_enum := 'Usage';
  demo_other_charge_type public.charge_type_enum := 'Other';
  demo_completed_status public.execution_status_enum := 'completed';
  demo_billing_account text := '564907680089';
BEGIN
  INSERT INTO public.users (user_id, email, username, password_hash, created_at)
  VALUES (
    demo_user_id,
    'demo@gmail.com',
    'demo@gmail.com',
    crypt('Password@2', gen_salt('bf', 12)),
    now()
  )
  ON CONFLICT DO NOTHING;

  PERFORM public.create_new_tenant(demo_user_id);

  -- Cloud Connection & Account
  INSERT INTO public.cloud_connection (connection_id, user_id, provider, status)
  VALUES (
    demo_connection_id,
    demo_user_id,
    demo_provider,
    demo_status
  )
  ON CONFLICT (connection_id) DO NOTHING;

  INSERT INTO public.cloud_account (account_id, connection_id, account_type, ingestion_period, display_name)
  VALUES (
    demo_account_id,
    demo_connection_id,
    demo_account_type,
    demo_ingestion_period,
    'Test Account'
  )
  ON CONFLICT (account_id) DO NOTHING;
END $$;