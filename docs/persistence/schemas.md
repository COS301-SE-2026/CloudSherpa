# Database Schemas

This document is the schema reference for CloudSherpa persistence boundaries. It is strictly limited to:

- database schemas

Implementation notes, service behavior, deployment details, and analytics query examples belong in the relevant service or persistence documents, not here.

## Database Schemas

### SherpaDB

Source schema file:

`persistence/sherpadb/sherpadb-schema.sql`

SherpaDB is backed by PostgreSQL with TimescaleDB enabled for time-series storage.

#### `environment_reference`

Registry table for cloud environments/accounts known to SherpaDB.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `environment_id` | `UUID` | Primary key | Unique identifier for a connected cloud environment. |
| `provider` | `VARCHAR(50)` | `NOT NULL` | Cloud provider associated with the environment. |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` | Timestamp for when the environment reference was created. |

#### `normalized_metrics`

Time-series table for normalized usage and cost metrics.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `recorded_at` | `TIMESTAMPTZ` | `NOT NULL` | Timestamp for when the usage or cost was recorded. |
| `environment_id` | `UUID` | Foreign key to `environment_reference(environment_id)` | Environment associated with the metric. |
| `resource_id` | `VARCHAR(255)` | `NOT NULL` | Provider resource identifier. |
| `service_category` | `VARCHAR(100)` | `NOT NULL` | Normalized CloudSherpa service category. |
| `usage_amount` | `NUMERIC` | `NOT NULL` | Quantity of resource usage. |
| `usage_unit` | `VARCHAR(50)` | `NOT NULL` | Unit for `usage_amount`. |
| `cost_amount` | `NUMERIC` | `NOT NULL` | Cost associated with the usage record. |
| `currency` | `VARCHAR(10)` | `DEFAULT 'ZAR'` | Currency code for `cost_amount`. |

#### TimescaleDB Configuration

`normalized_metrics` is converted into a TimescaleDB hypertable partitioned by `recorded_at`.

```sql
SELECT create_hypertable('normalized_metrics', 'recorded_at');
```

The SherpaDB schema also defines an index for environment and time-based lookups.

```sql
CREATE INDEX ix_environment_time ON normalized_metrics (environment_id, recorded_at DESC);
```

## Schema Ownership

Database schema changes should update the relevant SQL schema file and this document in the same change.
