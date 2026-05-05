# Data Flow

This document describes the ingestion-to-analytics data flow and the PostgreSQL LISTEN/NOTIFY channel used by the service app.

## End-to-End Flow

1. **Ingestion** pulls raw usage data from cloud providers using `AwsConnector`.
2. **Normalization** maps provider data to the shared `NormalizedMetric` model via `AwsNormalizer`.
3. **Persistence** writes normalized rows to SherpaDB through `SherpaDbPersistenceService`.
4. **Database trigger** broadcasts the inserted row as JSON on the `metric_events` channel.
5. **Service listener** receives `metric_events` notifications and processes the payload.

## Component Responsibilities

### Ingestion

- Entry point: `CloudUsageService.ingestAndProcessMetrics()`
- Writes directly to SherpaDB (no writes in the service app).

### SherpaDB

- Schema file: `persistence/sherpadb/sherpadb-schema.sql`
- `normalized_metrics` inserts trigger `metric_notify_trigger`.

### Service

- `PostgresNotificationListener` maintains a dedicated JDBC connection.
- It executes `LISTEN metric_events` and polls for notifications on a schedule.
- The JSON payload is parsed and handed to analytics logic (future integration).

## LISTEN/NOTIFY Details

- Channel name: `metric_events`
- Payload format: JSON representation of the inserted `normalized_metrics` row.
- Trigger location: `metric_notify_trigger` in SherpaDB schema.

## Notes

- The pipeline is scheduler-ready. A future scheduler should call `ingestAndProcessMetrics()`.
- The service app is notify-only and does not query SherpaDB for reads or writes.
