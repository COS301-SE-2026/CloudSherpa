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
    sample_interval INTERVAL := INTERVAL '5 min';
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
            $5 - (i * $7) AS period_start,
            ($5 - (i * $7)) + $7 AS period_end
        FROM generate_series(1, $6) AS series(i)
        ON CONFLICT DO NOTHING
    $sql$, p_tenant_schema)
    USING
        p_metric_id,
        p_resource_id,
        p_metric_type,
        p_metric_name,
        p_to_timestamp,
        p_points,
        sample_interval;

    GET DIAGNOSTICS rows_inserted = ROW_COUNT;

    RAISE NOTICE 'Seeded % rows for resource %', rows_inserted, p_resource_id;
    RETURN rows_inserted;
END;
$$;

-- Creates the fixed AWS resources used by the NFR historical metric seed.
CREATE OR REPLACE FUNCTION seed_normalized_metrics(INTEGER)
RETURNS integer
LANGUAGE plpgsql AS
$$
DECLARE
    -- BE VERY CAREFUL CHANGING THESE, TESTS RELY ON THESE LITERALS
    tenant_schema text := 'tenant_5ebe4340_c5ec_4833_ad93_06abf4609f03';
    aws_account_id uuid := 'a0000000-0000-0000-0000-000000000001';
    resource_id_01 uuid := '10000000-0000-0000-0000-000000000001';
    resource_id_02 uuid := '10000000-0000-0000-0000-000000000002';
    resource_id_03 uuid := '10000000-0000-0000-0000-000000000003';

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

    to_timestamp timestamptz;
    metric_series_datapoints integer;
    total_rows_seeded integer := 0;

    resource_active_status public.status_enum := 'active';
    instance_id text := 'InstanceId';
    region text := 'eu-north-1';
    ec2 text := 'AWS/EC2';
BEGIN
    PERFORM seed_resource(
        tenant_schema, resource_id_01, aws_account_id,
        ec2, 'nfr-ec2-01', 'i-nfr000001', instance_id, region, resource_active_status
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_02, aws_account_id,
        ec2, 'nfr-ec2-02', 'i-nfr000002', instance_id, region, resource_active_status
    );
    PERFORM seed_resource(
        tenant_schema, resource_id_03, aws_account_id,
        ec2, 'nfr-ec2-03', 'i-nfr000003', instance_id, region, resource_active_status
    );
    RAISE NOTICE 'Resource Seeded, continuing to seed metrics';

    to_timestamp := NOW();
    metric_series_datapoints := $1;

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
        tenant_schema, metric_id_04, resource_id_03, metric_type_04, metric_name_04,
        to_timestamp, metric_series_datapoints
    );
    total_rows_seeded := total_rows_seeded + seed_metric_series(
        tenant_schema, metric_id_05, resource_id_03, metric_type_05, metric_name_05,
        to_timestamp, metric_series_datapoints
    );

    RAISE NOTICE 'Finished normalized metric seeding with % rows', total_rows_seeded;
    RETURN total_rows_seeded;
END;
$$;