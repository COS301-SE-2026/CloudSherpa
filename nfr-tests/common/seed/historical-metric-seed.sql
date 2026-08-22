-- Seeds one stable resource into a tenant schema.

CREATE OR REPLACE FUNCTION seed_resource(
    p_tenant_schema text,
    p_resource_id uuid,
    p_account_id uuid,
    p_resource_type text,
    p_resource_name text,
    p_resource_identifier text,
    p_resource_identifier_type text,
    p_region text,
    p_status public.status_enum
)
RETURNS void
LANGUAGE plpgsql AS
$$
BEGIN
    EXECUTE format($sql$
        INSERT INTO %I.resource (
            resource_id,
            account_id,
            resource_type,
            resource_name,
            resource_identifier,
            resource_identifier_type,
            region,
            status
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
        ON CONFLICT (account_id, resource_type, resource_identifier, region)
        -- Update name, status and last updated on conflicts i.e. reseed after already seeded
        DO UPDATE SET
            resource_name = EXCLUDED.resource_name,
            status = EXCLUDED.status,
            last_updated = NOW()
    $sql$, p_tenant_schema)
    USING
        p_resource_id,
        p_account_id,
        p_resource_type,
        p_resource_name,
        p_resource_identifier,
        p_resource_identifier_type,
        p_region,
        p_status;
END;
$$;

CREATE OR REPLACE FUNCTION seed_metric_series(
    p_tenant_schema text,
    p_metric_id uuid,
    p_resource_id uuid,
    p_metric_type text,
    p_metric_name text,
    p_to_timestamp timestamptz,
    p_points integer
)
RETURNS integer
LANGUAGE plpgsql AS
$$
DECLARE
    rows_inserted integer;
BEGIN 
    RAISE NOTICE 'Seeding metric series for resource %', p_resource_id;

    EXECUTE format($sql$
        INSERT INTO %I.normalized_metrics (
            metric_id,
            resource_id,
            recorded_at,
            metric_type,
            metric_name,
            metric_value,
            unit,
            currency,
            period_start,
            period_end
        )
        SELECT
            $1 AS metric_id,
            $2 AS resource_id,
            NOW() AS recorded_at,
            $3 AS metric_type,
            $4 AS metric_name,
            i AS metric_value,
            'NFR unit' AS unit,
            'USD' AS currency,
            $5 - (i * INTERVAL '5 min') AS period_start,
            ($5 - (i * INTERVAL '5 min')) + INTERVAL '5 min' AS period_end
        FROM generate_series(1, $6) AS series(i)
        ON CONFLICT DO NOTHING
    $sql$, p_tenant_schema)
    USING
        p_metric_id,
        p_resource_id,
        p_metric_type,
        p_metric_name,
        p_to_timestamp,
        p_points;

    GET DIAGNOSTICS rows_inserted = ROW_COUNT;

    RAISE NOTICE 'Seeded % rows for resource %', rows_inserted, p_resource_id;
    RETURN rows_inserted;
END;
$$;

-- Creates the fixed AWS resources used by the NFR historical metric seed.
CREATE OR REPLACE FUNCTION seed_normalized_metrics()
RETURNS integer
LANGUAGE plpgsql AS
$$
DECLARE
    tenant_schema text := 'tenant_5ebe4340_c5ec_4833_ad93_06abf4609f03';
    aws_account_id uuid := 'a0000000-0000-0000-0000-000000000001';
    resource_id_01 uuid := '10000000-0000-0000-0000-000000000001';
    resource_id_02 uuid := '10000000-0000-0000-0000-000000000002';
    resource_id_03 uuid := '10000000-0000-0000-0000-000000000003';
    resource_id_04 uuid := '10000000-0000-0000-0000-000000000004';
    resource_id_05 uuid := '10000000-0000-0000-0000-000000000005';
    resource_id_06 uuid := '10000000-0000-0000-0000-000000000006';
    resource_id_07 uuid := '10000000-0000-0000-0000-000000000007';
    resource_id_08 uuid := '10000000-0000-0000-0000-000000000008';
    resource_id_09 uuid := '10000000-0000-0000-0000-000000000009';
    resource_id_10 uuid := '10000000-0000-0000-0000-000000000010';

    metric_id_01 uuid := '20000000-0000-0000-0000-000000000001';
    metric_name_01 text := 'CPUUtilization';
    metric_type_01 text := 'cpu';
    metric_id_02 uuid := '20000000-0000-0000-0000-000000000002';
    metric_name_02 text := 'NetworkIn';
    metric_type_02 text := 'network';
    metric_id_03 uuid := '20000000-0000-0000-0000-000000000003';
    metric_name_03 text := 'NetworkOut';
    metric_type_03 text := 'network';
    metric_id_04 uuid := '20000000-0000-0000-0000-000000000004';
    metric_name_04 text := 'DiskReadBytes';
    metric_type_04 text := 'disk';
    metric_id_05 uuid := '20000000-0000-0000-0000-000000000005';
    metric_name_05 text := 'DiskWriteBytes';
    metric_type_05 text := 'disk';
    metric_id_06 uuid := '20000000-0000-0000-0000-000000000006';
    metric_name_06 text := 'DatabaseConnections';
    metric_type_06 text := 'connections';
    metric_id_07 uuid := '20000000-0000-0000-0000-000000000007';
    metric_name_07 text := 'ReadLatency';
    metric_type_07 text := 'latency';
    metric_id_08 uuid := '20000000-0000-0000-0000-000000000008';
    metric_name_08 text := 'Invocations';
    metric_type_08 text := 'invocations';
    metric_id_09 uuid := '20000000-0000-0000-0000-000000000009';
    metric_name_09 text := 'CPUUtilization';
    metric_type_09 text := 'cpu';
    metric_id_10 uuid := '20000000-0000-0000-0000-000000000010';
    metric_name_10 text := 'CPUUtilization';
    metric_type_10 text := 'cpu';

    to_timestamp timestamptz;
    metric_series_datapoints integer;
    total_rows_seeded integer := 0;
BEGIN
    PERFORM seed_resource(
        tenant_schema, resource_id_01, aws_account_id,
        'AWS/EC2', 'nfr-ec2-01', 'i-nfr000001', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_02, aws_account_id,
        'AWS/EC2', 'nfr-ec2-02', 'i-nfr000002', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_03, aws_account_id,
        'AWS/EC2', 'nfr-ec2-03', 'i-nfr000003', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_04, aws_account_id,
        'AWS/EC2', 'nfr-ec2-04', 'i-nfr000004', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_05, aws_account_id,
        'AWS/EC2', 'nfr-ec2-05', 'i-nfr000005', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_06, aws_account_id,
        'AWS/RDS', 'nfr-rds-01', 'nfr-rds-01', 'DBInstanceIdentifier', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_07, aws_account_id,
        'AWS/RDS', 'nfr-rds-02', 'nfr-rds-02', 'DBInstanceIdentifier', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_08, aws_account_id,
        'AWS/Lambda', 'nfr-lambda-01', 'nfr-lambda-01', 'FunctionName', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_09, aws_account_id,
        'AWS/ECS', 'nfr-ecs-cluster-01', 'nfr-ecs-cluster-01', 'ClusterName', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_10, aws_account_id,
        'AWS/ElastiCache', 'nfr-cache-01', 'nfr-cache-01', 'CacheClusterId', 'us-east-1', 'active'
    );

    RAISE NOTICE 'Resource Seeded, continuing to seed metrics';

    to_timestamp := NOW();
    metric_series_datapoints := 10000;

    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_01, resource_id_01, metric_type_01, metric_name_01,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_02, resource_id_02, metric_type_02, metric_name_02,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_03, resource_id_03, metric_type_03, metric_name_03,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_04, resource_id_04, metric_type_04, metric_name_04,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_05, resource_id_05, metric_type_05, metric_name_05,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_06, resource_id_06, metric_type_06, metric_name_06,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_07, resource_id_07, metric_type_07, metric_name_07,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_08, resource_id_08, metric_type_08, metric_name_08,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_09, resource_id_09, metric_type_09, metric_name_09,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_10, resource_id_10, metric_type_10, metric_name_10,
        to_timestamp, metric_series_datapoints
    );

    RAISE NOTICE 'Finished normalized metric seeding with % rows', total_rows_seeded;
    RETURN total_rows_seeded;
END;
$$;
