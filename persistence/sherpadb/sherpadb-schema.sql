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
CREATE TYPE public.theme_enum AS ENUM ('light', 'dark');
CREATE TYPE public.currency_enum AS ENUM ('USD', 'EUR', 'ZAR');
CREATE TYPE public.language_enum AS ENUM ('en', 'es', 'fr');
CREATE TYPE public.ingestion_period_enum AS ENUM ('1m', '5m', '1h');
CREATE TYPE public.predefined_time_enum AS ENUM (
  'T_5_MIN',
  'T_15_MIN',
  'T_30_MIN',
  'T_1_HOUR',
  'T_6_HOUR',
  'T_12_HOUR',
  'T_24_HOUR',
  'T_7_DAYS',
  'T_30_DAYS'
);
CREATE TYPE public.type_enum AS ENUM ('KPI', 'CHART');
CREATE TYPE public.execution_status_enum AS ENUM ('pending', 'processing', 'completed', 'failed');
CREATE TYPE PUBLIC.chart_type_enum AS ENUM ('gauge_chart', 'line_chart');
-- Differentiates actual compute usage from other types.
-- Maps to CUR: line_item_line_item_type
CREATE TYPE public.charge_type_enum AS ENUM ('Usage', 'Other'); 
CREATE TYPE public.optimization_status_enum AS ENUM (
  'DRAFT',
  'ACTIVE',
  'ACKNOWLEDGED',
  'DISMISSED',
  'APPLIED',
  'SUPERSEDED',
  'EXPIRED'
);

CREATE TYPE public.optimization_action_type_enum AS ENUM (
  'DOWNSIZE',
  'TERMINATE',
  'SUSPEND'
);

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
  created_at timestamptz DEFAULT NOW(),
  last_usage_ingestion timestamptz DEFAULT NOW(),
  next_usage_ingestion timestamptz DEFAULT NOW(),
  last_billing_ingestion timestamptz DEFAULT NOW(),
  next_billing_ingestion timestamptz DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS public.offered_metric (
    offered_metric_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    provider public.provider_enum NOT NULL,

    -- e.g. AWS/EC2, AWS/RDS, compute.googleapis.com/instance
    service_type varchar(255) NOT NULL,

    -- e.g. CPUUtilization, NetworkIn, cpu/utilization
    metric_name varchar(255) NOT NULL,

    -- Resource identifier dimension
    -- AWS examples:
    --   InstanceId
    --   DBInstanceIdentifier
    -- GCP examples:
    --   instance_id
    --   database_id
    identifier_field varchar(100) NOT NULL,

    -- Optional because AWS metrics already know their unit,
    -- while GCP does not.
    expected_unit varchar(50),

    description text,

    CONSTRAINT uq_offered_metric UNIQUE (
        provider,
        service_type,
        metric_name
    )
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
  bucket_region varchar(255) NOT NULL,
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

INSERT INTO public.offered_metric (
    provider,
    service_type,
    metric_name,
    identifier_field,
    expected_unit,
    description
)
VALUES
-- AWS EC2
('AWS', 'AWS/EC2', 'CPUUtilization', 'InstanceId', NULL, 'Average CPU utilization'),
('AWS', 'AWS/EC2', 'NetworkIn', 'InstanceId', NULL, 'Incoming network traffic'),
('AWS', 'AWS/EC2', 'NetworkOut', 'InstanceId', NULL, 'Outgoing network traffic'),
('AWS', 'AWS/EC2', 'DiskReadBytes', 'InstanceId', NULL, 'Bytes read from disks'),
('AWS', 'AWS/EC2', 'DiskWriteBytes', 'InstanceId', NULL, 'Bytes written to disks'),
('AWS', 'AWS/EC2', 'StatusCheckFailed', 'InstanceId', NULL, 'Instance status checks'),

-- AWS ECS
('AWS', 'AWS/ECS', 'CPUUtilization', 'ClusterName', NULL, 'Cluster CPU utilization'),
('AWS', 'AWS/ECS', 'MemoryUtilization', 'ClusterName', NULL, 'Cluster memory utilization'),
('AWS', 'AWS/ECS', 'CPUReservation', 'ClusterName', NULL, 'Reserved CPU'),
('AWS', 'AWS/ECS', 'MemoryReservation', 'ClusterName', NULL, 'Reserved memory'),

-- AWS EKS
('AWS', 'AWS/EKS', 'cluster_failed_request_count', 'ClusterName', NULL, 'Failed API requests'),
('AWS', 'AWS/EKS', 'cluster_node_count', 'ClusterName', NULL, 'Number of worker nodes'),
('AWS', 'AWS/EKS', 'cluster_request_total', 'ClusterName', NULL, 'API request count'),

-- AWS Lambda
('AWS', 'AWS/Lambda', 'Invocations', 'FunctionName', NULL, 'Function invocations'),
('AWS', 'AWS/Lambda', 'Errors', 'FunctionName', NULL, 'Function errors'),
('AWS', 'AWS/Lambda', 'Duration', 'FunctionName', NULL, 'Execution duration'),
('AWS', 'AWS/Lambda', 'ConcurrentExecutions', 'FunctionName', NULL, 'Concurrent executions'),
('AWS', 'AWS/Lambda', 'Throttles', 'FunctionName', NULL, 'Function throttles'),

-- AWS RDS
('AWS', 'AWS/RDS', 'CPUUtilization', 'DBInstanceIdentifier', NULL, 'CPU utilization'),
('AWS', 'AWS/RDS', 'DatabaseConnections', 'DBInstanceIdentifier', NULL, 'Open database connections'),
('AWS', 'AWS/RDS', 'FreeStorageSpace', 'DBInstanceIdentifier', NULL, 'Remaining storage'),
('AWS', 'AWS/RDS', 'ReadLatency', 'DBInstanceIdentifier', NULL, 'Read latency'),
('AWS', 'AWS/RDS', 'WriteLatency', 'DBInstanceIdentifier', NULL, 'Write latency'),
('AWS', 'AWS/RDS', 'FreeableMemory', 'DBInstanceIdentifier', NULL, 'Available memory'),

-- AWS ElastiCache
('AWS', 'AWS/ElastiCache', 'CPUUtilization', 'CacheClusterId', NULL, 'CPU utilization'),
('AWS', 'AWS/ElastiCache', 'CurrConnections', 'CacheClusterId', NULL, 'Current connections'),
('AWS', 'AWS/ElastiCache', 'Evictions', 'CacheClusterId', NULL, 'Evicted keys'),
('AWS', 'AWS/ElastiCache', 'FreeableMemory', 'CacheClusterId', NULL, 'Available memory'),
('AWS', 'AWS/ElastiCache', 'NetworkBytesIn', 'CacheClusterId', NULL, 'Incoming network bytes'),
('AWS', 'AWS/ElastiCache', 'NetworkBytesOut', 'CacheClusterId', NULL, 'Outgoing network bytes'),

-- AWS OpenSearch
('AWS', 'AWS/OpenSearch', 'CPUUtilization', 'DomainName', NULL, 'CPU utilization'),
('AWS', 'AWS/OpenSearch', 'JVMMemoryPressure', 'DomainName', NULL, 'JVM memory pressure'),
('AWS', 'AWS/OpenSearch', 'FreeStorageSpace', 'DomainName', NULL, 'Available storage'),
('AWS', 'AWS/OpenSearch', 'ClusterIndexWritesBlocked', 'DomainName', NULL, 'Index writes blocked'),
('AWS', 'AWS/OpenSearch', 'SearchLatency', 'DomainName', NULL, 'Search latency'),

-- AWS Redshift
('AWS', 'AWS/Redshift', 'CPUUtilization', 'ClusterIdentifier', NULL, 'CPU utilization'),
('AWS', 'AWS/Redshift', 'HealthStatus', 'ClusterIdentifier', NULL, 'Cluster health'),
('AWS', 'AWS/Redshift', 'DatabaseConnections', 'ClusterIdentifier', NULL, 'Database connections'),
('AWS', 'AWS/Redshift', 'PercentageDiskSpaceUsed', 'ClusterIdentifier', NULL, 'Disk utilization'),
('AWS', 'AWS/Redshift', 'ReadIOPS', 'ClusterIdentifier', NULL, 'Read IOPS'),
('AWS', 'AWS/Redshift', 'WriteIOPS', 'ClusterIdentifier', NULL, 'Write IOPS')

ON CONFLICT DO NOTHING;

DO $$
DECLARE
    -- GCP service types
    c_gcp_gce_service CONSTANT varchar(255) := 'gce_instance';
    c_gcp_gke_service CONSTANT varchar(255) := 'gke_cluster';
    c_gcp_cloud_function_service CONSTANT varchar(255) := 'cloud_function';
    c_gcp_cloud_run_service CONSTANT varchar(255) := 'cloud_run_service';
    c_gcp_gcs_service CONSTANT varchar(255) := 'gcs_bucket';

    -- GCP identifier fields
    c_gcp_instance_id CONSTANT varchar(100) := 'instance_id';
    c_gcp_cluster_name CONSTANT varchar(100) := 'cluster_name';
    c_gcp_function_name CONSTANT varchar(100) := 'function_name';
    c_gcp_service_name CONSTANT varchar(100) := 'service_name';
    c_gcp_bucket_name CONSTANT varchar(100) := 'bucket_name';

    -- GCP units
    c_gcp_percent_unit CONSTANT varchar(50) := '10^2.%';
    c_gcp_count_unit CONSTANT varchar(50) := 'Count';
    c_gcp_bytes_unit CONSTANT varchar(50) := 'By';
    c_gcp_seconds_unit CONSTANT varchar(50) := 's';
    c_gcp_milliseconds_unit CONSTANT varchar(50) := 'ms';
BEGIN

    INSERT INTO public.offered_metric (
        provider,
        service_type,
        metric_name,
        identifier_field,
        expected_unit,
        description
    )
    VALUES
    -- GCP Compute Engine (VM Instances)
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/cpu/utilization',
 c_gcp_instance_id, c_gcp_percent_unit, 'CPU utilization'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/cpu/reserved_cores',
 c_gcp_instance_id, c_gcp_count_unit, 'Reserved CPU cores'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/network/received_bytes_count',
 c_gcp_instance_id, c_gcp_bytes_unit, 'Network bytes received'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/network/sent_bytes_count',
 c_gcp_instance_id, c_gcp_bytes_unit, 'Network bytes sent'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/disk/read_bytes_count',
 c_gcp_instance_id, c_gcp_bytes_unit, 'Disk bytes read'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/disk/write_bytes_count',
 c_gcp_instance_id, c_gcp_bytes_unit, 'Disk bytes written'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/disk/read_ops_count',
 c_gcp_instance_id, c_gcp_count_unit, 'Disk read operations'),
('GCP', c_gcp_gce_service, 'compute.googleapis.com/instance/disk/write_ops_count',
 c_gcp_instance_id, c_gcp_count_unit, 'Disk write operations'),

-- GKE (Clusters)
('GCP', c_gcp_gke_service, 'kubernetes.io/node/cpu/core_usage_time',
 c_gcp_cluster_name, c_gcp_seconds_unit, 'CPU usage time'),
('GCP', c_gcp_gke_service, 'kubernetes.io/node/memory/used_bytes',
 c_gcp_cluster_name, c_gcp_bytes_unit, 'Memory used'),
('GCP', c_gcp_gke_service, 'kubernetes.io/node/network/received_bytes_count',
 c_gcp_cluster_name, c_gcp_bytes_unit, 'Network bytes received'),
('GCP', c_gcp_gke_service, 'kubernetes.io/node/network/sent_bytes_count',
 c_gcp_cluster_name, c_gcp_bytes_unit, 'Network bytes sent'),
('GCP', c_gcp_gke_service, 'kubernetes.io/pod/restart_count',
 c_gcp_cluster_name, c_gcp_count_unit, 'Pod restart count'),

-- Cloud Functions
('GCP', c_gcp_cloud_function_service,
 'cloudfunctions.googleapis.com/function/execution_count',
 c_gcp_function_name, c_gcp_count_unit, 'Function executions'),
('GCP', c_gcp_cloud_function_service,
 'cloudfunctions.googleapis.com/function/execution_times',
 c_gcp_function_name, c_gcp_milliseconds_unit, 'Execution time'),
('GCP', c_gcp_cloud_function_service,
 'cloudfunctions.googleapis.com/function/user_memory_bytes',
 c_gcp_function_name, c_gcp_bytes_unit, 'Memory usage'),
('GCP', c_gcp_cloud_function_service,
 'cloudfunctions.googleapis.com/function/active_instances',
 c_gcp_function_name, c_gcp_count_unit, 'Active instances'),

-- Cloud Run
('GCP', c_gcp_cloud_run_service,
 'run.googleapis.com/request_count',
 c_gcp_service_name, c_gcp_count_unit, 'HTTP requests'),
('GCP', c_gcp_cloud_run_service,
 'run.googleapis.com/request_latencies',
 c_gcp_service_name, c_gcp_milliseconds_unit, 'Request latency'),
('GCP', c_gcp_cloud_run_service,
 'run.googleapis.com/container/cpu/utilizations',
 c_gcp_service_name, c_gcp_percent_unit, 'CPU utilization'),
('GCP', c_gcp_cloud_run_service,
 'run.googleapis.com/container/memory/utilizations',
 c_gcp_service_name, c_gcp_percent_unit, 'Memory utilization'),
('GCP', c_gcp_cloud_run_service,
 'run.googleapis.com/container/instance_count',
 c_gcp_service_name, c_gcp_count_unit, 'Running instances'),

-- Cloud Storage
('GCP', c_gcp_gcs_service,
 'storage.googleapis.com/storage/total_bytes',
 c_gcp_bucket_name, c_gcp_bytes_unit, 'Stored bytes'),
('GCP', c_gcp_gcs_service,
 'storage.googleapis.com/api/request_count',
 c_gcp_bucket_name, c_gcp_count_unit, 'API requests'),
('GCP', c_gcp_gcs_service,
 'storage.googleapis.com/network/received_bytes_count',
 c_gcp_bucket_name, c_gcp_bytes_unit, 'Bytes uploaded'),
('GCP', c_gcp_gcs_service,
 'storage.googleapis.com/network/sent_bytes_count',
 c_gcp_bucket_name, c_gcp_bytes_unit, 'Bytes downloaded')
    ON CONFLICT DO NOTHING;
END $$;

-- This sits in the public schema so it only has to be written once, but it is 
-- smart enough to broadcast on a specific tenant's channel dynamically.
CREATE TABLE IF NOT EXISTS public.kpi_charges (
  kpi_charges_id uuid PRIMARY KEY,
  widget_kpi_id uuid NOT NULL REFERENCES public.widget_kpi(kpi_id) ON DELETE CASCADE,
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
  resource_id uuid, 
  metric_type varchar(50)
);

-- ----------------------------------------------------------------
-- GLOBAL FUNCTIONS
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.notify_metric_event()
RETURNS TRIGGER AS $$
DECLARE
    source_schema text := TG_TABLE_SCHEMA;
    tenant_channel text;
BEGIN
    SELECT h.hypertable_schema
    INTO source_schema
    FROM timescaledb_information.chunks c
    JOIN timescaledb_information.hypertables h
      ON h.hypertable_schema = c.hypertable_schema
     AND h.hypertable_name = c.hypertable_name
    WHERE c.chunk_schema = TG_TABLE_SCHEMA
      AND c.chunk_name = TG_TABLE_NAME
    LIMIT 1;

    tenant_channel := 'metric_events_' || COALESCE(source_schema, TG_TABLE_SCHEMA);

    PERFORM pg_notify('metric_events', row_to_json(NEW)::text);
    PERFORM pg_notify(tenant_channel, row_to_json(NEW)::text);

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
            resource_identifier varchar(255) NOT NULL,
            resource_identifier_type varchar(255) NOT NULL,
            region varchar(100) NOT NULL,
            status public.status_enum,
            tags jsonb,
            last_updated timestamptz DEFAULT NOW(),
            created_at timestamptz DEFAULT NOW(),

            CONSTRAINT uq_resource_identity
        UNIQUE (
            account_id,
            resource_type,
            resource_identifier,
            region
        )
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
            metric_type varchar(50) NOT NULL,
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
            cost_id text,
            
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

    -- --------------------------------------------------------------------------
    -- Optimization Tables
    -- --------------------------------------------------------------------------

    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.optimization_metric_statistics (
            statistics_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
            resource_id uuid NOT NULL REFERENCES %I.resource(resource_id) ON DELETE CASCADE,
            provider public.provider_enum NOT NULL,
            metric_name varchar(255) NOT NULL,
            window_num_days integer NOT NULL,

            minimum_value numeric,
            maximum_value numeric,
            average_value numeric,
            median_value numeric,
            p95_value numeric,
            p99_value numeric,
            standard_deviation numeric,
            spike_count integer DEFAULT 0,
            peak_duration_seconds integer DEFAULT 0,
            completeness_ratio numeric,

            window_start timestamptz NOT NULL,
            window_end timestamptz NOT NULL,
            calculated_at timestamptz DEFAULT NOW()
        );
    $sql$, schema_name, schema_name);

    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.optimization_recommendation (
            recommendation_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
            resource_id uuid NOT NULL REFERENCES %I.resource(resource_id) ON DELETE CASCADE,
            provider public.provider_enum NOT NULL,
            rule_id varchar(255) NOT NULL,
            action_type public.optimization_action_type_enum NOT NULL,
            status public.optimization_status_enum NOT NULL DEFAULT 'DRAFT',
            evidence jsonb DEFAULT '{}'::jsonb,
            created_at timestamptz DEFAULT NOW(),
            updated_at timestamptz DEFAULT NOW(),
            expires_at timestamptz
        );
    $sql$, schema_name, schema_name);

    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.recommendation_history (
            history_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
            recommendation_id uuid NOT NULL REFERENCES %I.optimization_recommendation(recommendation_id),
            resource_id uuid NOT NULL REFERENCES %I.resource(resource_id) ON DELETE CASCADE,
            provider public.provider_enum NOT NULL,
            rule_id varchar(255) NOT NULL,
            action_type public.optimization_action_type_enum NOT NULL,
            previous_status public.optimization_status_enum,
            new_status public.optimization_status_enum NOT NULL,
            evidence jsonb DEFAULT '{}'::jsonb,
            changed_at timestamptz DEFAULT NOW()
        );
    $sql$, schema_name, schema_name, schema_name);

    EXECUTE format($sql$
        CREATE TABLE IF NOT EXISTS %I.processing_watermark (
            pipeline_name varchar(255) PRIMARY KEY,
            last_processed_period timestamptz,
            last_successful_run timestamptz,
            updated_at timestamptz DEFAULT NOW()
        );
    $sql$, schema_name);

END;
$$ LANGUAGE plpgsql;

\set demo_password ''
\getenv demo_password DEMO_PASSWORD

-- Custom variable
-- Must be separated by a .
SET sherpa.demo_password = :'demo_password';

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
    crypt(current_setting('sherpa.demo_password'), gen_salt('bf', 12)),
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
