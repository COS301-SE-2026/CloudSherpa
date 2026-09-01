CREATE FUNCTION seed_user(
    p_nfr_user_id uuid,
    p_nfr_user_email text,
    p_nfr_user_username text,
    p_nfr_user_password text,
    p_nfr_account_id uuid,
    p_nfr_connection_id uuid,
    p_nfr_provider public.provider_enum,
    p_nfr_status public.status_enum,
    p_nfr_account_type public.account_type_enum,
    p_nfr_ingestion_period public.ingestion_period_enum,
    p_nfr_account_display_name text
) RETURNS void LANGUAGE plpgsql AS
$$
BEGIN
INSERT INTO public.users (user_id, email, username, password_hash, created_at)
VALUES (
    p_nfr_user_id,
    p_nfr_user_email,
    p_nfr_user_username,
    crypt(p_nfr_user_password, gen_salt('bf', 12)),
    NOW()
) ON CONFLICT (user_id) DO UPDATE SET
    email = EXCLUDED.email,
    username = EXCLUDED.username,
    password_hash = EXCLUDED.password_hash;

PERFORM public.create_new_tenant(p_nfr_user_id);

INSERT INTO public.cloud_connection (connection_id, user_id, provider, status)
VALUES (
    p_nfr_connection_id,
    p_nfr_user_id,
    p_nfr_provider,
    p_nfr_status
)
ON CONFLICT (connection_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    provider = EXCLUDED.provider,
    status = EXCLUDED.status;

INSERT INTO public.cloud_account (account_id, connection_id, account_type, ingestion_period, display_name)
VALUES (
    p_nfr_account_id,
    p_nfr_connection_id,
    p_nfr_account_type,
    p_nfr_ingestion_period,
    p_nfr_account_display_name
)
ON CONFLICT (account_id) DO UPDATE SET
    connection_id = EXCLUDED.connection_id,
    account_type = EXCLUDED.account_type,
    ingestion_period = EXCLUDED.ingestion_period,
    display_name = EXCLUDED.display_name;

END $$;

SELECT seed_user(
    'a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246',
    'nfr-test-user@nfr-test.com',
    'nfr-test-user',
    'nfr-test-pass@123!',
    '06f744fd-76e5-4845-9780-ced666c26ffe',
    '8b271aa3-a6e6-4ddc-befe-cbff69cf4020',
    'AWS',
    'active',
    'aws_account',
    '1h',
    'NFR Test Account'
);