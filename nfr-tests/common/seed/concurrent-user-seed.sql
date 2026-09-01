-- Assumes that the following scripts have already ran
-- common.sql
-- nfr-user.sql
-- historical-metric-seed.sql

CREATE FUNCTION seed_multiple_users() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    current_user_id uuid;
    current_connection_id uuid;
    current_account_id uuid;
BEGIN
    FOR i IN 1...50 LOOP
        current_user_id := gen_random_uuid();
        current_connection_id := gen_random_uuid();
        current_account_id := gen_random_uuid();

        SELECT seed_user(
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
    END LOOP;
END;
$$
