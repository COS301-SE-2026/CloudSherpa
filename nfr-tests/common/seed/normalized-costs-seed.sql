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

    RAISE NOTICE 'Seeded AWS export execution';
END;
$$;
-- normalized costs
CREATE OR REPLACE FUNCTION seed_normalized_costs() 
RETURNS void
LANGUAGE plpgsql AS 
$$
DECLARE
    tenant_schema text := 'tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246';
    aws_account_id uuid := '06f744fd-76e5-4845-9780-ced666c26ffe';

    billing_export_config_id uuid := 'c181db1b-e20b-4b34-a606-af13a2d48524';
    billing_export_execution_id uuid := '9b3602b5-bf53-4de3-a5d9-3704734f8255';

BEGIN
    PERFORM seed_aws_billing_config(aws_account_id, billing_export_config_id);
    PERFORM seed_billing_export_execution(billing_export_config_id, billing_export_execution_id);

END;
$$;
