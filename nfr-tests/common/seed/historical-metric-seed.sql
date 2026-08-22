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


    RETURN 0;
END;
$$;
