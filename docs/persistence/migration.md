# Schema Migration

### Tool - Flyway

---

## Migration Ownership
Flyway will run from a dedicated database migration job rather than from the service or ingestion application.

Both applications connect to the same PostgreSQL/TimescaleDB database and start independently. Assigning Flyway to either application would couple schema readiness to an unrelated runtime service.

The dedicated migration job is the single owner of database schema changes. The service and ingestion applications must not execute Flyway migrations.

---

## Concurrent Application Startup
Applications must start only after the migration job completes successfully.

* The migration job runs before the service and ingestion applications.
* The service and ingestion applications depend on the migration job completing successfully.
* If the migration job fails, neither application should start.
* Flyway's database lock prevents concurrent migration execution.
* Only one migration job may be active for a database environment at a time.
* Applications must not independently retry or execute migrations.

---

## Migration Naming and Versioning
Flyway migrations use versioned SQL files stored in:

    persistence/sherpadb/db/migration/

Migration filenames follow this convention:

    V<version>__<description>.sql

**Examples:**
* `V1__create_initial_schema.sql`
* `V2__add_billing_indexes.sql`
* `V3__create_cost_summary_view.sql`

**Naming Rules:**
* Versions are positive integers.
* Versions must be unique.
* Versions must increase for every new migration.
* Descriptions use lowercase words separated by underscores.
* Applied migrations must never be edited.
* A new migration must be created to correct or extend an existing migration.
* Every migration must be reviewed before it is merged.

---

## Production Migration Failures
A production deployment must be considered unsuccessful if the migration job fails.

**When a migration fails:**
* Flyway must report the failure and return a non-zero exit code.
* The deployment must stop before starting or updating application containers.
* The failure must be investigated before another deployment attempt.
* The failed migration must not be edited after it has been applied in any environment.
* A corrective migration should be created when the database is in a recoverable state.
* `flyway repair` may only be run by an authorized maintainer after confirming the migration state and database changes.

---

## Documentation Requirements
Every schema change must include:
* The purpose of the change
* The affected tables, functions, indexes, or other database objects
* Any data transformation or backfill behavior
* Compatibility considerations for running application versions

> **Note:** This document is the repository source of truth for the Flyway ownership, startup, versioning and failure policies.