-- Assumes common.sql has been run first to define shared seed helpers.

-- billing config
CREATE OR REPLACE FUNCTION seed_aws_billing_config(
    p_account_id uuid,
    p_billing_export_config_id uuid
) RETURNS void LANGUAGE plpgsql AS $$
-- provider specific billing config
BEGIN 

    RAISE NOTICE 'Seeding AWS billing config';

    INSERT INTO public.billing_export_config (config_id, account_id, created_at)
    VALUES (p_billing_export_config_id, p_account_id, NOW())
    ON CONFLICT (config_id) DO UPDATE SET
    config_id = EXCLUDED.config_id,
    created_at = EXCLUDED.created_at;

    INSERT INTO public.aws_billing_export_config
        (
            config_id,
            bucket_name,
            bucket_region,
            export_prefix,
            export_name
        )
        VALUES 
        (
            p_billing_export_config_id,
            'somebucket',
            'af-south-1',
            'someprefix',
            'somename'
        ) ON CONFLICT (config_id) DO UPDATE SET
            bucket_name = EXCLUDED.bucket_name,
            bucket_region = EXCLUDED.bucket_region,
            export_prefix = EXCLUDED.export_prefix,
            export_name = EXCLUDED.export_name;

    RAISE NOTICE 'Seeded AWS billing config';
END;
$$;
-- billing export execution
CREATE OR REPLACE FUNCTION seed_billing_export_execution(
    p_billing_export_config_id uuid,
    p_billing_export_execution_id uuid
)
RETURNS void
LANGUAGE plpgsql AS 
$$
DECLARE
    billing_export_execution_status public.execution_status_enum := 'completed';
BEGIN 

    RAISE NOTICE 'Seeding AWS billing export execution';

    INSERT INTO public.billing_export_execution
    (
        execution_id,
        config_id,
        status,
        rows_processed,
        started_at,
        completed_at,
        error_message
    )
    VALUES (
        p_billing_export_execution_id,
        p_billing_export_config_id,
        billing_export_execution_status,
        1000,
        NOW(),
        NOW(),
        ''
    ) ON CONFLICT (execution_id) DO UPDATE SET
        config_id = EXCLUDED.config_id,
        status = EXCLUDED.status,
        rows_processed = EXCLUDED.rows_processed,
        started_at = EXCLUDED.started_at,
        completed_at = EXCLUDED.completed_at,
        error_message = EXCLUDED.error_message;

    RAISE NOTICE 'Seeded AWS billing export execution';
END;
$$;
-- normalized costs

CREATE OR REPLACE FUNCTION populate_normalized_costs(
    p_tenant_schema text,
    p_cost_id text,
    p_execution_id uuid,
    p_resource_id VARCHAR(2048),
    p_charge_id VARCHAR(2048),
    p_service_name VARCHAR(255),
    p_to_timestamp timestamptz,
    p_points integer
) RETURNS integer LANGUAGE plpgsql AS $$ 
DECLARE
    rows_inserted integer;
    charge_type public.charge_type_enum := 'Usage';
    currency public.currency_enum := 'USD';
    seed_provider public.provider_enum := 'AWS';
    billing_account_id varchar(255) := 'Account Holder 1';
    sample_interval INTERVAL := INTERVAL '1 hour';
BEGIN
    RAISE NOTICE 'Seeding normalized costs for charge %', p_charge_id;
    EXECUTE format($sql$
        INSERT INTO %I.normalized_costs (
            cost_id,
            execution_id,
            resource_id,
            charge_id,
            provider,
            billing_account_id,
            service_name,
            charge_type,
            cost_amount,
            currency,
            usage_start_time,
            usage_end_time
        )
        SELECT
            $1 AS cost_id,
            $2 AS execution_id,
            $3 AS resource_id,
            $4 AS charge_id,
            $5 AS provider,
            $6 AS billing_account_id,
            $7 AS service_name,
            $8 AS charge_type,
            i::numeric(16, 8) AS cost_amount,
            $9 AS currency,
            $10 - (i * $11) AS usage_start_time,
            ($10 - (i * $11)) + $11 AS usage_end_time
        FROM generate_series(1, $12) AS series(i)
        ON CONFLICT DO NOTHING
    $sql$, p_tenant_schema)
    USING
        p_cost_id,
        p_execution_id,
        p_resource_id,
        p_charge_id,
        seed_provider,
        billing_account_id,
        p_service_name,
        charge_type,
        currency,
        p_to_timestamp,
		sample_interval,
        p_points;

    GET DIAGNOSTICS rows_inserted = ROW_COUNT;
    RAISE NOTICE 'Seeded normalized costs for charge % with % rows', p_charge_id, rows_inserted;

    RETURN rows_inserted;

END;
$$;

CREATE OR REPLACE FUNCTION seed_normalized_costs() 
RETURNS void
LANGUAGE plpgsql AS 
$$
DECLARE
    tenant_schema text := 'tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246';
    aws_account_id uuid := '06f744fd-76e5-4845-9780-ced666c26ffe';

    billing_export_config_id uuid := 'c181db1b-e20b-4b34-a606-af13a2d48524';
    billing_export_execution_id uuid := '9b3602b5-bf53-4de3-a5d9-3704734f8255';

    normalized_costs_seed_points integer := 100;

    service_name_01 VARCHAR(255) := 'AWSDataTransfer';
    resource_id_01 VARCHAR(2048) := 'i-0000000000';
    charge_id_01 VARCHAR(2048) := 'i-0000000000%%%AWSDataTransfer';
    cost_id_01 text := 'seed-cost-id-01'; 

    service_name_02 VARCHAR(255) := 'AmazonEC2';
    resource_id_02 VARCHAR(2048) := 'i-0000000001';
    charge_id_02 VARCHAR(2048) := 'i-0000000001%%%AmazonEC2';
    cost_id_02 text := 'seed-cost-id-02';

    service_name_03 VARCHAR(255) := 'AmazonRDS';
    resource_id_03 VARCHAR(2048) := 'db-0000000001';
    charge_id_03 VARCHAR(2048) := 'db-0000000001%%%AmazonRDS';
    cost_id_03 text := 'seed-cost-id-03';
BEGIN
    PERFORM seed_aws_billing_config(aws_account_id, billing_export_config_id);
    PERFORM seed_billing_export_execution(billing_export_config_id, billing_export_execution_id);

    PERFORM populate_normalized_costs(
        tenant_schema, cost_id_01, billing_export_execution_id, resource_id_01, charge_id_01, service_name_01, NOW(), normalized_costs_seed_points
    );
    PERFORM populate_normalized_costs(
        tenant_schema, cost_id_02, billing_export_execution_id, resource_id_02, charge_id_02, service_name_02, NOW(), normalized_costs_seed_points
    );
    PERFORM populate_normalized_costs(
        tenant_schema, cost_id_03, billing_export_execution_id, resource_id_03, charge_id_03, service_name_03, NOW(), normalized_costs_seed_points
    );
END;
$$;
