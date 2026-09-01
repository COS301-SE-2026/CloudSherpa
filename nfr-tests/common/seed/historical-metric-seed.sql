-- Assumes common.sql has been run first to define shared seed helpers.

CREATE OR REPLACE FUNCTION seed_metric_series(
    p_tenant_schema text,
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
    sample_interval INTERVAL := '5 min';
BEGIN 
    RAISE NOTICE 'Seeding metric series for resource %', p_resource_id;

    EXECUTE format($sql$
        INSERT INTO %I.normalized_metrics (
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
            $1 AS resource_id,
            NOW() AS recorded_at,
            $2 AS metric_type,
            $3 AS metric_name,
            i AS metric_value,
            'NFR unit' AS unit,
            'USD' AS currency,
            $4 - (i * $5) AS period_start,
            ($4 - (i * $5)) + $5 AS period_end
        FROM generate_series(1, $6) AS series(i)
        ON CONFLICT DO NOTHING
    $sql$, p_tenant_schema)
    USING
        p_resource_id,
        p_metric_type,
        p_metric_name,
        p_to_timestamp,
        sample_interval,
        p_points;

    GET DIAGNOSTICS rows_inserted = ROW_COUNT;

    RAISE NOTICE 'Seeded % rows for resource %', rows_inserted, p_resource_id;
    RETURN rows_inserted;
END;
$$;

-- Creates the fixed AWS resources used by the NFR historical metric seed.
CREATE OR REPLACE FUNCTION seed_normalized_metrics(p_tenant_schema text, p_account_id uuid, p_num_seed integer)
RETURNS integer
LANGUAGE plpgsql AS
$$
DECLARE
    -- p_tenant_schema text := 'tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246';
    -- p_account_id uuid := '06f744fd-76e5-4845-9780-ced666c26ffe';
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

    metric_name_01 text := 'CPUUtilization';
    metric_type_01 text := 'cpu';
    metric_name_02 text := 'NetworkIn';
    metric_type_02 text := 'network';
    metric_name_03 text := 'NetworkOut';
    metric_type_03 text := 'network';
    metric_name_04 text := 'DiskReadBytes';
    metric_type_04 text := 'disk';
    metric_name_05 text := 'DiskWriteBytes';
    metric_type_05 text := 'disk';
    metric_name_06 text := 'DatabaseConnections';
    metric_type_06 text := 'connections';
    metric_name_07 text := 'ReadLatency';
    metric_type_07 text := 'latency';
    metric_name_08 text := 'Invocations';
    metric_type_08 text := 'invocations';
    metric_name_09 text := 'CPUUtilization';
    metric_type_09 text := 'cpu';
    metric_name_10 text := 'CPUUtilization';
    metric_type_10 text := 'cpu';

    to_timestamp timestamptz;
    metric_series_datapoints integer;
    total_rows_seeded integer := 0;
BEGIN
    PERFORM seed_resource(
        p_tenant_schema, resource_id_01, p_account_id,
        'AWS/EC2', 'nfr-ec2-01', 'i-nfr000001', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_02, p_account_id,
        'AWS/EC2', 'nfr-ec2-02', 'i-nfr000002', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_03, p_account_id,
        'AWS/EC2', 'nfr-ec2-03', 'i-nfr000003', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_04, p_account_id,
        'AWS/EC2', 'nfr-ec2-04', 'i-nfr000004', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_05, p_account_id,
        'AWS/EC2', 'nfr-ec2-05', 'i-nfr000005', 'InstanceId', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_06, p_account_id,
        'AWS/RDS', 'nfr-rds-01', 'nfr-rds-01', 'DBInstanceIdentifier', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_07, p_account_id,
        'AWS/RDS', 'nfr-rds-02', 'nfr-rds-02', 'DBInstanceIdentifier', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_08, p_account_id,
        'AWS/Lambda', 'nfr-lambda-01', 'nfr-lambda-01', 'FunctionName', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_09, p_account_id,
        'AWS/ECS', 'nfr-ecs-cluster-01', 'nfr-ecs-cluster-01', 'ClusterName', 'us-east-1', 'active'
    );
    PERFORM seed_resource(
        p_tenant_schema, resource_id_10, p_account_id,
        'AWS/ElastiCache', 'nfr-cache-01', 'nfr-cache-01', 'CacheClusterId', 'us-east-1', 'active'
    );

    RAISE NOTICE 'Resource Seeded, continuing to seed metrics';

    to_timestamp := NOW();
    metric_series_datapoints := p_num_seed;
    
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_01, metric_type_01, metric_name_01,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_02, metric_type_02, metric_name_02,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_03, metric_type_03, metric_name_03,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_04, metric_type_04, metric_name_04,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_05, metric_type_05, metric_name_05,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_06, metric_type_06, metric_name_06,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_07, metric_type_07, metric_name_07,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_08, metric_type_08, metric_name_08,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_09, metric_type_09, metric_name_09,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        p_tenant_schema, resource_id_10, metric_type_10, metric_name_10,
        to_timestamp, metric_series_datapoints
    );

    RAISE NOTICE 'Finished normalized metric seeding with % rows', total_rows_seeded;
    RETURN total_rows_seeded;
END;
$$;
