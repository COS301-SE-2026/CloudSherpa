# Analytics Database

The analytics database is powered by **TimescaleDB**, a time-series extension for PostgreSQL. It is designed to store normalized cloud usage and cost metrics for time-based analytics workloads.

For the canonical table definitions, column types, constraints, hypertable configuration, and indexes, see [Database Schemas](schemas.md#analytics-database).

## Purpose

The analytics database supports queries over standardized billing and usage records after they have been normalized from provider-specific formats.

It is used to:

- store normalized cloud usage and cost records
- associate usage records with connected cloud environments
- support time-windowed analytics queries
- support environment-specific cost and usage lookups

## Storage Model

Analytics data is stored as time-series data. The schema is optimized around recorded usage time and environment-specific lookups.

Schema details are intentionally kept out of this page to avoid duplicate documentation. Update [Database Schemas](schemas.md#analytics-database) whenever the analytics database schema changes.
