DO
$$
DECLARE
    nfr_user_id uuid := 'a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246';
    nfr_user_email text := 'nfr-test-user@nfr-test.com';
    nfr_user_username text := 'nfr-test-user';
    nfr_user_password text := 'nfr-test-pass@123!';
    nfr_account_id uuid := '06f744fd-76e5-4845-9780-ced666c26ffe';
    nfr_connection_id uuid := '8b271aa3-a6e6-4ddc-befe-cbff69cf4020';
    nfr_provider public.provider_enum := 'AWS';
    nfr_status public.status_enum := 'active';
    nfr_account_type public.account_type_enum := 'aws_account';
    nfr_ingestion_period public.ingestion_period_enum := '1h';
    nfr_account_display_name text := 'NFR Test Account';
BEGIN
INSERT INTO public.users (user_id, email, username, password_hash, created_at)
VALUES (
    nfr_user_id,
    nfr_user_email,
    nfr_user_username,
    crypt(nfr_user_password, gen_salt('bf', 12)),
    NOW()
) ON CONFLICT (user_id) DO UPDATE SET
    email = EXCLUDED.email,
    username = EXCLUDED.username,
    password_hash = EXCLUDED.password_hash;

PERFORM public.create_new_tenant(nfr_user_id);

INSERT INTO public.cloud_connection (connection_id, user_id, provider, status)
VALUES (
    nfr_connection_id,
    nfr_user_id,
    nfr_provider,
    nfr_status
)
ON CONFLICT (connection_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    provider = EXCLUDED.provider,
    status = EXCLUDED.status;

INSERT INTO public.cloud_account (account_id, connection_id, account_type, ingestion_period, display_name)
VALUES (
    nfr_account_id,
    nfr_connection_id,
    nfr_account_type,
    nfr_ingestion_period,
    nfr_account_display_name
)
ON CONFLICT (account_id) DO UPDATE SET
    connection_id = EXCLUDED.connection_id,
    account_type = EXCLUDED.account_type,
    ingestion_period = EXCLUDED.ingestion_period,
    display_name = EXCLUDED.display_name;

END $$;
