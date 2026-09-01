-- Assumes that the following scripts have already ran
-- common.sql
-- nfr-user.sql
-- historical-metric-seed.sql

CREATE FUNCTION uuid_to_tenant_schema(user_uuid uuid) RETURNS text LANGUAGE plpgsql AS
$$
BEGIN 
    RETURN 'tenant_' || REPLACE(user_uuid::text, '-', '_');
END;
$$;

CREATE FUNCTION seed_multiple_users() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    current_user_id uuid;
    current_connection_id uuid;
    current_account_id uuid;
    tenant_schema text;
BEGIN
    FOR i IN 1..50 LOOP
        current_user_id := gen_random_uuid();
        current_connection_id := gen_random_uuid();
        current_account_id := gen_random_uuid();

        PERFORM seed_user(
            current_user_id,
            i || '-nfr-test-user@nfr-test.com',
            i || 'nfr-test-user',
            'nfr-test-pass@123!',
            current_account_id,
            current_connection_id,
            'AWS',
            'active',
            'aws_account',
            '1h',
            i || ' NFR Test Account'
        );
        tenant_schema := uuid_to_tenant_schema(current_user_id);
        PERFORM seed_normalized_metrics(tenant_schema, current_account_id, 10);

    END LOOP;
END;
$$;

SELECT seed_multiple_users();