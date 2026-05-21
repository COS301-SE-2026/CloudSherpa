# SherpaDB ERD and Persistence Overview

This document describes the ERD used for CloudSherpa persistence, and how it maps to the PostgreSQL and TimescaleDB implementation.

ERD diagram: [docs/assets/diagrams/images/SherpaDB-ERD.png](docs/assets/diagrams/images/SherpaDB-ERD.png)

Schema file: [persistence/sherpadb/sherpadb-schema.sql](persistence/sherpadb/sherpadb-schema.sql)

## Enums

| Enum Name            | Allowed Values                      |
|----------------------|-------------------------------------|
| provider_enum        | `AWS`, `AZURE`, `GCP`               |
| status_enum          | `active`, `disabled`                |
| credential_type_enum | `access_key`, `oauth`               |
| account_type_enum    | `aws_account`, `azure_subscription`, `gcp_project` |
| metric_type_enum     | `cost`, `usage`, `performance`      |
|||

## Tables

### users
Purpose: system users who own cloud connections.

| Column Name    | Data Type      | Key/Constraint | Description                                                      |
|--------------- |---------------|----------------|------------------------------------------------------------------|
| user_id        | UUID           | Primary Key    | Unique identifier.|
| email          | VARCHAR(320)   | Unique         | User's unique login identity.                                    |
| username       | VARCHAR(100)   | Not Null       | Display name for UI rendering.                                   |
| password_hash  | VARCHAR(255)   | Not Null       | Hashed password for authentication								  |
| created_at     | TIMESTAMPTZ    | Default NOW()  | Creation timestamp in UTC.                                       |
|||||

### cloud_connection
Purpose: a logical link between a user and a cloud provider.

| Column Name    | Data Type      | Key/Constraint     | Description                                                      |
|--------------- |---------------|--------------------|------------------------------------------------------------------|
| connection_id  | UUID           | Primary Key        | Identifier for the connection.                            |
| user_id        | UUID           | Foreign Key        | Links to `users.user_id`.                                          |
| provider       | provider_enum  | Not Null           | Selects which provider connector to use for ingestion.           |
| status         | status_enum    | Default `active`   | Used as a toggle to block ingestion jobs without deleting the connection. |
| created_at     | TIMESTAMPTZ    | Default NOW()      | Creation timestamp in UTC.                                       |
|||||

### cloud_credential
Purpose: store provider-specific credentials.

| Column Name      | Data Type            | Key/Constraint     | Description                                                      |
|------------------|---------------------|--------------------|------------------------------------------------------------------|
| credential_id    | UUID                | Primary Key        | Unique credential record.                                        |
| connection_id    | UUID                | Foreign Key        | Links to `cloud_connection.connection_id`.                         |
| provider         | provider_enum        | Not Null           | Provider designation for easier querying.               |
| credential_type  | credential_type_enum | Not Null           | Defines the authentication method.                               |
| credential_value | TEXT                | Not Null           | Encrypted JSON payload stored as a string.                       |
| created_at       | TIMESTAMPTZ         | Default NOW()      | Creation timestamp.                                              |
|||||

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

| Column Name    | Data Type           | Key/Constraint     | Description                                                      |
|--------------- |--------------------|--------------------|------------------------------------------------------------------|
| account_id     | UUID                | Primary Key        | Primary key for the boundary.                                    |
| connection_id  | UUID                | Foreign Key        | Links to `cloud_connection.connection_id`.                         |
| account_type   | account_type_enum   | Not Null           | Classifies the boundary type (e.g., AWS Account vs. GCP Project).|
| display_name   | VARCHAR(255)        | Nullable           | User-friendly label for UI dashboards.                           |
| created_at     | TIMESTAMPTZ         | Default NOW()      | When the account was registered.                                 |
|||||

### resource
Purpose: individual cloud assets measured (instances, buckets, disks, etc.).


| Column Name    | Data Type      | Key/Constraint     | Description                                                      |
|--------------- |---------------|--------------------|------------------------------------------------------------------|
| resource_id    | UUID           | Primary Key        | Unique identifier for the resource.                              |
| account_id     | UUID           | Foreign Key        | Links to `cloud_account.account_id`.                               |
| resource_type  | VARCHAR(255)   | Nullable           | Provider-specific asset type.                                    |
| tags           | VARCHAR(255)   | Nullable           | Key/value metadata stored as text.                               |
| created_at     | TIMESTAMPTZ    | Default NOW()      | First time the resource was discovered by ingestion.             |
|||||

### normalized_metrics
Purpose: normalized time-series measurements for cost, usage, and performance.


| Column Name    | Data Type           | Key/Constraint     | Description                                                      |
|--------------- |--------------------|--------------------|------------------------------------------------------------------|
| recorded_at    | TIMESTAMPTZ        | Composite PK       | Time the metric was ingested. 									   |
| metric_id      | UUID                | Composite PK       | Unique identifier for the metric event.                          |
| account_id     | UUID                | Foreign Key        | Links to `cloud_account.account_id`.                              |
| resource_id    | UUID                | Foreign Key        | Links to `resource.resource_id` 								   |
| metric_type    | metric_type_enum    | Not Null           | Broad categorization of the metric.                              |
| metric_name    | VARCHAR(255)        | Not Null           | Standardized name (e.g., CPUUtilization, NetworkIn).             |
| metric_value   | NUMERIC             | Not Null           | Exact numeric value. NUMERIC prevents floating-point drift (critical for costs). |
| unit           | VARCHAR(50)         | Nullable           | Unit of measurement (Percent, Bytes, USD).                       |
| currency       | VARCHAR(10)         | Nullable           | Currency code (populated only if metric_type is cost).           |
| period_start   | TIMESTAMPTZ         | Nullable           | Start of the measurement window.                                 |
| period_end     | TIMESTAMPTZ         | Nullable           | End of the measurement window.                                   |
|||||

## Advanced Database Features

### TimescaleDB Partitioning & Indexing

To handle massive scale efficiently, standard PostgreSQL tables are augmented:

| Feature              | Implementation                                                                         | Description                                                                                       |
|----------------------|----------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| Hypertable Conversion | `SELECT create_hypertable('normalized_metrics', 'recorded_at');`                        | Converts the standard table into a hypertable, abstracting automatic time-based partitioning behind the scenes. |
| Performance Indexing | `CREATE INDEX ix_resource_time ON normalized_metrics (resource_id, recorded_at DESC);` | Optimizes highly common UI queries by preventing full table scans when filtering by resource and ordering by newest data. |

### Real-Time event Broadcasting

SherpaDB leverages PostgreSQL's native LISTEN/NOTIFY system to trigger downstream application logic the moment new metrics are written to the database.

#### The Trigger
`CREATE TRIGGER metric_notify_trigger AFTER INSERT ON normalized_metrics FOR EACH ROW...` ensures that if a batch of 100 rows is inserted, the trigger fires exactly 100 times.

#### The Function
`notify_metric_event()` packages the newly inserted row (NEW) into a JSON object via `row_to_json()`.

#### The Broadcast
It executes `PERFORM pg_notify('metric_events', ...)` to push the JSON string to the `metric_events` channel.

#### Example Broadcast Payload
Applications listening to the `metric_events` channel receive payloads formatted like this immediately after a successful insertion:

``` 
{
  "metric_id": "a1b2c3d4-e5f6-7a8b-9c0d-1234567890ab",
  "account_id": "f5e4d3c2-b1a0-9f8e-7d6c-ba0987654321",
  "recorded_at": "2026-05-18T13:26:24.000Z",
  "resource_id": "c1d2e3f4-a5b6-c7d8-e9f0-1234567890cd",
  "metric_type": "performance",
  "metric_name": "CPUUtilization",
  "metric_value": 85.5,
  "unit": "Percent",
  "currency": null,
  "period_start": "2026-05-18T13:20:00.000Z",
  "period_end": "2026-05-18T13:25:00.000Z"
}
```