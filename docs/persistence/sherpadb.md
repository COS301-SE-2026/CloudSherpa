# SherpaDB Overview

SherpaDB is CloudSherpa's TimescaleDB-backed PostgreSQL database for time-series cost and usage metrics.

## Schema Source

- Schema file: `persistence/sherpadb/sherpadb-schema.sql`
- Tables: `environment_reference`, `normalized_metrics`

## Notifications

SherpaDB emits `NOTIFY` payloads on the `metric_events` channel after inserts into `normalized_metrics`.
The service app listens to these notifications and processes the payloads without querying the database.
