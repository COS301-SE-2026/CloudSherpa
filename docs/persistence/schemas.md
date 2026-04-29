# Database and Kafka Schemas

This document is the schema reference for CloudSherpa persistence boundaries. It is strictly limited to:

- database schemas
- Kafka topic schemas
- Avro schema files used by Kafka producers and consumers

Implementation notes, service behavior, deployment details, and analytics query examples belong in the relevant service or persistence documents, not here.

!!! warning "Mock Kafka schema"
    The current `cloud_usage_event.avsc` schema is a mock schema. It does not reflect the real ingestion data structure.

    It exists to demonstrate how schemas can be structured and to support testing Kafka configurations, producers, and consumers.

## Database Schemas

### Analytics Database

Source schema file:

`persistence/analytics/analytics-schema.sql`

The analytics database is backed by PostgreSQL with TimescaleDB enabled for time-series storage.

#### `environment_reference`

Registry table for cloud environments/accounts known to the analytics database.

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

The analytics schema also defines an index for environment and time-based lookups.

```sql
CREATE INDEX ix_environment_time ON normalized_metrics (environment_id, recorded_at DESC);
```

## Kafka Schemas

### `cloud_usage_event.avsc`

Current schema files:

- `libs/kafka/schemas/cloud_usage_event.avsc`
- `apps/ingestion-service/src/main/avro/cloud_usage_event.avsc`
- `apps/normalization-service/src/main/avro/cloud_usage_event.avsc`

Record name:

`com.cloudsherpa.events.CloudUsageEvent`

!!! warning "Mock schema only"
    This schema is currently a mock schema. It should not be treated as the canonical structure of ingestion data.

    Use it only as a reference for Avro schema layout and for testing Kafka producer/consumer wiring.

#### Fields

| Field | Avro Type | Description |
|-------|-----------|-------------|
| `provider` | `string` | Mock cloud provider identifier. |
| `accountId` | `string` | Mock cloud account identifier. |
| `serviceName` | `string` | Mock provider service name. |
| `usageAmount` | `double` | Mock usage quantity. |
| `cost` | `double` | Mock cost value. |
| `currency` | `string` | Mock currency code. |
| `timestamp` | `long` | Mock event timestamp. |

#### Intended Use

The current `cloud_usage_event.avsc` can be used to:

- validate Kafka schema configuration
- generate test producers and consumers
- test serialization and deserialization
- demonstrate how future Avro schemas should be structured in the repository

It should not be used to make assumptions about the final ingestion payload shape.

## Schema Ownership

Database schema changes should update the relevant SQL schema file and this document in the same change.

Kafka schema changes should update all required Avro schema locations and this document in the same change.
