# AnalyticsDB Schema Documentation

This database is powered by **TimescaleDB** (a time-series extension for PostgreSQL). It is specifically designed to ingest, store, and query massive amounts of standardized cloud billing data very quickly.

## Tables

### `environment_reference`

A lookup table that acts as a registry for the connected cloud accounts. It provides the analytics engine with just enough context to group and compare costs across different cloud platforms.

**Fields:**

| Field | Type | Constraints | Purpose |
|-------|------|-------------|---------|
| `environment_id` | UUID | Primary Key | Unique system identifier for a connected cloud account. |
| `provider` | VARCHAR(50) | NOT NULL | Standardized cloud provider name (e.g., `AWS`, `GCP`, `AZURE`). Enables easy calculation and comparison of spending across different clouds. |
| `created_at` | TIMESTAMPTZ | DEFAULT NOW() | Timestamp when this environment was first registered. Useful for system auditing and debugging. |

---

### `normalized_metrics`

Stores the standardized output from the **Normalizer** service. Takes the chaotic, varied billing formats from AWS, GCP, and Azure and unifies them into one standardized structure.

**Fields:**

| Field | Type | Constraints | Purpose |
|-------|------|-------------|---------|
| `recorded_at` | TIMESTAMPTZ | NOT NULL | The exact date/time the cost or usage occurred. TimescaleDB uses it to organize and retrieve data efficiently. |
| `environment_id` | UUID | Foreign Key (`environment_reference`) | Links back to the cloud account. Answers: "Which cloud account incurred this cost?" |
| `resource_id` | VARCHAR(255) | NOT NULL | Unique identifier from the cloud provider (e.g., AWS EC2 instance ID `i-0abcd1234`, GCP bucket name). Enables tracking costs down to individual resources. |
| `service_category` | VARCHAR(100) | NOT NULL | CloudSherpa standardized service grouping (e.g., `Compute`, `Storage`, `Networking`). Allows cross-cloud aggregation regardless of provider-specific terminology. |
| `usage_amount` | NUMERIC | NOT NULL | Raw volume of consumption (e.g., `730`, `50.5`). Tracks how much of a resource was used. |
| `usage_unit` | VARCHAR(50) | NOT NULL | Unit of measurement (e.g., `vCPU-hours`, `GB-months`). Gives `usage_amount` real-world meaning. |
| `cost_amount` | NUMERIC | NOT NULL | Actual money billed for this line item. Tracks financial spend. |
| `currency` | VARCHAR(10) | DEFAULT 'ZAR' | Currency code for the cost. Defaults to South African Rand (ZAR). Ensures financial data is tracked accurately. |


## Database Optimizations

### Hypertable

**SQL:**
```sql
SELECT create_hypertable('normalized_metrics', 'recorded_at');
```

**What it does:** Converts the standard `normalized_metrics` table into a **TimescaleDB Hypertable**, automatically partitioning it by time.

**Why we need it:** This chops the massive table into smaller, time-based chunks. When the application queries data for "last week", the database doesn't scan years of history, it just grabs the specific chunks for last week, resulting in massive performance gains.

### Composite Index

**SQL:**
```sql
CREATE INDEX ix_environment_time ON normalized_metrics (environment_id, recorded_at DESC);
```

**What it does:** Creates an index organized first by account ID, then by time in descending order (newest first).

**Why we need it:** When a user opens their dashboard, the system immediately queries for their account's most recent data. This index allows the database to instantly jump to exactly that subset of data without scanning the entire table.