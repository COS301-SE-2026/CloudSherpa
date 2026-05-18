# SherpaDB ERD and Persistence Overview

This document describes the ERD used for CloudSherpa persistence, and how it maps to the system.

ERD diagram: [docs/assets/diagrams/images/SherpaDB-ERD.png](docs/assets/diagrams/images/SherpaDB-ERD.png)

## Notes on Current SQL

The SQL schema in [persistence/sherpadb/sherpadb-schema.sql](persistence/sherpadb/sherpadb-schema.sql) is a starting point and will be updated to match the ERD as the persistence layer is normalized.

## Enums

Use enums for these fields to keep values consistent:

- provider: AWS, AZURE, GCP
- status: active, disabled, error
- credential_type: access_key, oauth
- account_type: aws_account, azure_subscription, gcp_project
- metric_type: cost, usage, performance

## Tables

### user
Purpose: system users who own cloud connections.

- user_id (UUID): primary key; UUID avoids collisions across distributed services.
- email (VARCHAR): unique login identity; text fits provider-agnostic emails.
- username (VARCHAR): display name; text is sufficient and flexible.
- password_hash (VARCHAR): hashed password for authentication; never store raw passwords.
- created_at (TIMESTAMPTZ): creation timestamp in UTC.

Usage: owner and access control for connections and their data.

### cloud_connection
Purpose: a logical link between a user and a cloud provider.

- connection_id (UUID): primary key; stable identifier for the connection.
- user_id (UUID FK): link to the owning user.
- provider (ENUM): AWS, AZURE, or GCP.
- status (ENUM): active, disabled, error; used to block ingestion when needed.
- created_at (TIMESTAMPTZ): creation timestamp in UTC.

Usage: selects which provider connector to use for ingestion.

### cloud_credential
Purpose: store provider-specific credentials.

- credential_id (UUID): primary key; unique credential record.
- connection_id (UUID FK): which connection uses these credentials.
- provider (ENUM): provider the credentials belong to.
- credential_type (ENUM): access_key, oauth.
- credential_value (TEXT): encrypted credential payload stored in the DB.
- created_at (TIMESTAMPTZ): when the credential was created.

Usage: connectors decrypt `credential_value` at runtime to call provider APIs.

#### Credential payload format
`credential_value` should store a single encrypted JSON object so multi-field credential sets stay together (for example, access key ID + secret + region). This keeps provider-specific shapes in one place while the database schema stays normalized.

Example (unencrypted payload before encryption):

```json
{
	"access_key_id": "someRandomSequence",
	"secret_access_key": "someEncryptedSequence",
	"region": "AF-SOUTH-1"
}
```

### cloud_account
Purpose: the data boundary where usage/cost is billed and collected.

- account_id (UUID): primary key for the boundary.
- connection_id (UUID FK): which connection can access this account.
- account_type (ENUM): aws_account, azure_subscription, gcp_project.
- display_name (VARCHAR): user-friendly label for UI.
- created_at (TIMESTAMPTZ): when the account was registered.

Usage: accounts group resources and metrics for attribution and filtering.

### resource
Purpose: individual cloud assets measured (instances, buckets, disks, etc.).

- resource_id (UUID): primary key for the resource record.
- account_id (UUID FK): which account the resource belongs to.
- resource_type (VARCHAR): provider-specific type (instance, bucket, disk, etc.).
- tags (JSONB): key/value metadata; JSONB handles sparse, provider-specific tags.
- created_at (TIMESTAMPTZ): first time the resource was seen.

Usage: metrics are recorded against resources to support per-asset views.

### normalized_metrics
Purpose: normalized time-series measurements for cost, usage, and performance.

- metric_id (UUID): primary key for a metric record.
- recorded_at (TIMESTAMPTZ): time the metric was ingested or stored.
- resource_id (UUID FK): target resource for the metric.
- metric_type (ENUM): cost, usage, or performance.
- metric_name (VARCHAR): CPUUtilization, NetworkIn, UsageCost, etc.
- metric_value (NUMERIC): numeric value; NUMERIC avoids floating-point drift.
- unit (VARCHAR): unit for metric_value (Percent, Bytes, USD, etc.).
- currency (VARCHAR): currency code for cost metrics only.
- period_start (TIMESTAMPTZ): start of measurement window.
- period_end (TIMESTAMPTZ): end of measurement window.

Usage: the dashboard builds charts by `(resource_id, metric_type, metric_name)`.
