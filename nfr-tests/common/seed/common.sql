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
