# Infrastructure

Local infrastructure and container orchestration for CloudSherpa.

## Contents

- `docker-compose.yml` - local Docker Compose stack for Kafka, Kafka topic initialization, Schema Registry, and CloudSherpa app containers.

## Prerequisites

- Docker Engine
- Docker Compose v2
- App-level `.env` files initialized with `scripts/env-init.sh`

Initialize environment files once before starting the stack:

```bash
cd scripts
chmod +x env-init.sh
./env-init.sh
```

The script is idempotent and does not overwrite existing `.env` files. Once it has been run, the Docker Compose stack should have the local `.env` files it needs. Services without a `.env.example` need one added before the script can initialize their `.env`.

## Start Shared Dependencies

Use this when running app dev servers directly on the host. This starts Kafka, runs `kafka-init` to create local topics, and starts Schema Registry:

```bash
docker compose -f infra/docker-compose.yml up -d --build kafka kafka-init schema-registry
```

Host connection values:

- Kafka: `localhost:29092`
- Schema Registry: `http://localhost:9000`

Docker-network connection values:

- Kafka: `kafka:9092`
- Schema Registry: `http://schema-registry:8081`

Kafka bootstrap server values are broker addresses and do not include `http://`. Schema Registry values are HTTP URLs and should include the `http://` scheme.

## AnalyticsDB (TimescaleDB)

CloudSherpa’s AnalyticsDB runs as a dedicated TimescaleDB container named `analytics-db`. The `analytics-engine` container can reach it over the Docker network.

### Before you start

1. Make sure you have a repo-root `.env` file. It should define:

   - `POSTGRES_DB`
   - `POSTGRES_USER`
   - `POSTGRES_PASSWORD`
2. Run the commands below from the repository root.

### Start AnalyticsDB

1. **First time only:** Reset the volume to ensure init scripts run:
   ```bash
   docker compose -f infra/docker-compose.yml down -v

2. Start the database:

   ```bash
   docker compose -f infra/docker-compose.yml up -d analytics-db
   docker compose -f infra/docker-compose.yml ps
   ```

   On first startup, initialization scripts automatically run:
   - TimescaleDB extension is enabled
   - Base schemas and tables are created via `persistence/analytics/analytics-schema.sql`

3. Connect to the database if needed:
   Currently using `psql`: The command-line interface (CLI) client for PostgreSQL

   ```bash
   docker exec -it analytics-db sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
   ```

   You can run additional SQL commands here. Exit `psql` with `\q`.

4. Verify the schema was created successfully:

   Inside psql, run these commands to verify:

   **List all tables:**
   ```sql
   \dt
   ```

   Expected output:
   ```
                List of relations
    Schema |        Name        | Type  |   Owner
   --------+-----------------------+-------+----------
    public | environment_reference | table | cloudsherpa
    public | normalized_metrics    | table | cloudsherpa
   (2 rows)
   ```

   **View the normalized_metrics table structure:**
   ```sql
   \d normalized_metrics
   ```

   Expected output:
   ```
                             Table "public.normalized_metrics"
         Column      |           Type           | Collation | Nullable | Default
   -------------------+--------------------------+-----------+----------+---------
    recorded_at       | timestamp with time zone |           | not null |
    environment_id    | uuid                     |           |          |
    resource_id       | character varying(255)   |           | not null |
    service_category  | character varying(100)   |           | not null |
    usage_amount      | numeric                  |           | not null |
    usage_unit        | character varying(50)    |           | not null |
    cost_amount       | numeric                  |           | not null |
    currency          | character varying(10)    |           |          | 'ZAR'::character varying
   Indexes:
       "ix_environment_time" btree (environment_id, recorded_at DESC)
   ```

   **Verify TimescaleDB hypertable:**
   ```sql
   SELECT * FROM timescaledb_information.hypertables;
   ```

   Expected output:
   ```
    hypertable_schema | hypertable_name    | owner
   -------------------+--------------------+----------
    public            | normalized_metrics | cloudsherpa
   (1 row)
   ```

   **Check row count (should be empty initially):**
   ```sql
   SELECT COUNT(*) FROM normalized_metrics;
   ```

   Expected output:
   ```
    count
   -------
        0
   (1 row)
   ```

   Exit psql with `\q`.

### Container-to-container connectivity

- From containers in this Compose stack: use `analytics-db:5432`
- From the host machine (because the port is published): use `localhost:5432`

### Persistence and resets

AnalyticsDB data is persisted via a named Docker volume (so data survives container restarts).

To reset the stack and remove volumes (destructive; deletes DB data):

```bash
docker compose -f infra/docker-compose.yml down -v
```

## Start the Full Stack

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up --build
```

Run in the background:

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

## Start One Service

```bash
docker compose -f infra/docker-compose.yml up --build dashboard-frontend
```

Replace `dashboard-frontend` with any service name from the compose file.

## Start Selected Services

```bash
docker compose -f infra/docker-compose.yml up --build dashboard-frontend dashboard-backend
```

Replace `dashboard-frontend` and `dashboard-backend` with any service name from the compose file.


## Stop the Stack

```bash
docker compose -f infra/docker-compose.yml down
```

Stop and remove anonymous volumes:

```bash
docker compose -f infra/docker-compose.yml down -v
```

## Logs

The following commands assume that containers are running.

Follow all logs:

```bash
docker compose -f infra/docker-compose.yml logs -f
```

Follow one service:

```bash
docker compose -f infra/docker-compose.yml logs -f kafka
```

## Service Ports

| Service | Host Port | Container Port | Notes |
| --- | ---: | ---: | --- |
| `dashboard-frontend` | `3000` | `3000` | Next.js frontend |
| `dashboard-backend` | `3001` | `3001` | NestJS API |
| `alert-engine` | `3002` | `3000` | Express API |
| `intelligence-engine` | `8000` | `8000` | FastAPI service |
| `kafka-init` | n/a | n/a | One-shot Kafka topic initialization |
| `ingestion-service` | `8081` | `8080` | Spring Boot service |
| `normalization-service` | `8082` | `8080` | Spring Boot service |
| `analytics-engine` | `8083` | `8080` | Spring Boot service |
| `schema-registry` | `9000` | `8081` | Confluent Schema Registry |
| `kafka` | `29092` | `29092` | External listener for host clients |

## Kafka Listener Model

The local Kafka container exposes two client paths:

- `localhost:29092` for applications running on the host.
- `kafka:9092` for applications running inside Docker Compose.

Use the correct address for the runtime location of the client. A service running in Docker Compose should not use `localhost:29092` to reach Kafka.

`kafka-init` uses the Docker-network address by default and exits after creating configured topics. Re-running it is safe when topics already exist.

## Schema Registry and Avro Schemas

Schema Registry stores registered schemas for Kafka clients at runtime. The source files for CloudSherpa Kafka processing schemas are Avro `.avsc` files under `libs/kafka/schemas`.

Spring Boot Kafka producers and consumers generate Java Avro classes from `apps/<service>/src/main/avro`, so copy the required files from `libs/kafka/schemas` into that service-local directory before building or running the service. This copy step is manual for now and should be automated later.

## Development vs Production

### Development

- Uses single-node Kafka in KRaft mode.
- Uses plaintext Kafka listeners.
- Uses local `.env` files under each app directory.
- Builds app images from local source.
- Intended for local integration testing, not production reliability.

### Production
(Planned, not yet implemented)
- Use managed or hardened Kafka with authentication, TLS, backups, and monitoring.
- Inject secrets through the deployment platform or a secret manager.
- Do not mount local `.env` files into production containers.
- Build immutable images through CI.
- Pin deployment image tags instead of relying on `latest`.
- Add health checks, resource limits, restart policies, and observability before production deployment.

## Troubleshooting

Check running containers:

```bash
docker compose -f infra/docker-compose.yml ps
```

Rebuild a service after dependency changes:

```bash
docker compose -f infra/docker-compose.yml build --no-cache dashboard-backend
```

Remove stopped containers and networks for this stack:

```bash
docker compose -f infra/docker-compose.yml down
```
