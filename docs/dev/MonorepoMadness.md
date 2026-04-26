# Monorepo Madness

This document explains where things live in the CloudSherpa repository and where new files should go. It is intentionally focused on directory. For service-specific setup, commands, ports, dependencies, and environment details, use the service READMEs under `apps/`.

## Repository Tree

_Generated on 26/04_. Should update regularly or automate via local git lifecycle hooks or github actions.

```text
.
|-- .env.example
|-- .gitignore
|-- README.md
|-- apps
|   |-- alert-engine
|   |-- analytics-engine
|   |-- dashboard-backend
|   |-- dashboard-frontend
|   |-- ingestion-service
|   |-- intelligence-engine
|   |-- kafka-init
|   `-- normalization-service
|-- docs
|   |-- assets
|   |   `-- team-photos
|   |       |-- TeamLogo.png
|   |       `-- TeamPhoto.png
|   `-- dev
|       |-- CheatSheet.md
|       |-- MonorepoMadness.md
|       `-- UsingKafka.md
|-- infra
|   |-- README.md
|   `-- docker-compose.yml
|-- libs
|   `-- kafka
|       `-- example_schema.avsc
`-- scripts
    |-- README.md
    |-- dev-dependencies.sh
    |-- env-init.sh
    `-- spring-init.sh
```

## Top-Level Directories

| Path | Purpose |
| --- | --- |
| `apps/` | Independently runnable services. Each service owns its own source code, dependencies, Dockerfile, environment example, and README. |
| `docs/` | Project documentation and supporting assets. |
| `infra/` | Infrastructure and orchestration files, currently centered on Docker Compose. |
| `libs/` | Shared contracts and reusable files used across service boundaries. |
| `scripts/` | Setup and maintenance scripts used by developers. |

Keep root files minimal. If a file can reasonably live in one of the directories above, place it there instead of at the repository root.

## `apps/`

Each folder in `apps/` is a runtime unit that can be built, run, tested, and deployed separately.

Use `apps/` for code that:

- has its own process or server;
- owns a clear system responsibility;
- has its own dependency manifest;
- has its own Dockerfile;
- has its own `.env.example`;
- communicates with other services through explicit interfaces such as HTTP or Kafka.

Do not put shared utility code in `apps/` just because one service uses it first. If it becomes a cross-service contract or reusable module, move it under `libs/`.

### Standard Service Shape

```text
service-name/
|-- Dockerfile
|-- .env.example
|-- README.md
|-- src/
|-- tests/
`-- package.json / pom.xml / requirements.txt
```

Not every service will have every folder on day one, but new services should follow this shape as they grow.

### Service Index

Use these READMEs for service-specific setup, development commands, build commands, ports, dev dependencies, and environment guidance.

| Service | README |
| --- | --- |
| Alert Engine | [`apps/alert-engine/README.md`](../../apps/alert-engine/README.md) |
| Analytics Engine | [`apps/analytics-engine/README.md`](../../apps/analytics-engine/README.md) |
| Dashboard Backend | [`apps/dashboard-backend/README.md`](../../apps/dashboard-backend/README.md) |
| Dashboard Frontend | [`apps/dashboard-frontend/README.md`](../../apps/dashboard-frontend/README.md) |
| Ingestion Service | [`apps/ingestion-service/README.md`](../../apps/ingestion-service/README.md) |
| Intelligence Engine | [`apps/intelligence-engine/README.md`](../../apps/intelligence-engine/README.md) |
| Kafka Init | [`apps/kafka-init/README.md`](../../apps/kafka-init/README.md) |
| Normalization Service | [`apps/normalization-service/README.md`](../../apps/normalization-service/README.md) |

## `docs/`

Documentation belongs under `docs/`.

Use:

- `docs/dev/` for developer-facing guides;
- `docs/assets/` for images, diagrams, screenshots, PDFs, and other static documentation assets.

Current developer docs:

| File | Purpose |
| --- | --- |
| [`docs/dev/CheatSheet.md`](CheatSheet.md) | Quick command reference for local development. |
| [`docs/dev/MonorepoMadness.md`](MonorepoMadness.md) | Repository structure and file placement guide. |
| [`docs/dev/UsingKafka.md`](UsingKafka.md) | Kafka-specific development notes. |

## `infra/`

Infrastructure files live in `infra/`.

Current files:

| File | Purpose |
| --- | --- |
| [`infra/docker-compose.yml`](../../infra/docker-compose.yml) | Local container stack for Kafka, Schema Registry, and app services. |
| [`infra/README.md`](../../infra/README.md) | Infrastructure commands, ports, environment notes, and Docker Compose usage. |

Put container orchestration, local infrastructure wiring, and future deployment-adjacent configuration here. Do not put application source code in `infra/`.

## `libs/`

Shared cross-service contracts and reusable files belong in `libs/`.

Use `libs/` for:

- Kafka schemas;
- shared message contracts;
- shared validation rules;
- generated or shared types used by more than one service;
- cross-service utilities that are not owned by a single app.

Do not use `libs/` for service-specific business logic. If only one service uses it and owns it, keep it inside that service.

### `libs/kafka/`

Kafka-related shared definitions live under `libs/kafka/`.

Current files:

| File | Purpose |
| --- | --- |
| [`libs/kafka/example_schema.avsc`](../../libs/kafka/example_schema.avsc) | Example Avro schema for shared Kafka message contracts. |

Schema changes should be coordinated with every producer and consumer that uses the schema.

## `scripts/`

Developer scripts belong in `scripts/`.

Refer to [`scripts/README.md`](../../scripts/README.md) 

Scripts should be repeatable where possible, documented in `scripts/README.md`, and scoped to setup or maintenance tasks that developers actually run more than once.

## Root Files

Root files should be limited to project-level entry points and configuration that genuinely applies to the whole repository.

| File | Purpose |
| --- | --- |
| `README.md` | Main user-facing project overview and run instructions. |
| `.env.example` | Repository-level environment template, if shared values are needed. |
| `.gitignore` | Ignore rules for generated files, local secrets, dependencies, and build outputs. |

## Environment Files

Environment templates should live next to the code that consumes them.

- Service-specific variables belong in `apps/<service>/.env.example`.
- Shared repository-level variables may belong in root `.env.example`.
- Real `.env` files are local and should not be committed.
- Run `scripts/env-init.sh` once to create local `.env` files from existing `.env.example` files.

Once `scripts/env-init.sh` has been run, local app and Docker Compose commands should have the expected `.env` files in place.

## Adding Files

Use this quick placement guide:

| If you are adding... | Put it in... |
| --- | --- |
| A new independently runnable service | `apps/<service-name>/` |
| Service-specific source code | `apps/<service-name>/src/` |
| Service-specific tests | `apps/<service-name>/tests/` or the framework's test folder |
| Service-specific setup docs | `apps/<service-name>/README.md` |
| Docker Compose or local stack wiring | `infra/` |
| Shared Kafka schemas | `libs/kafka/` |
| Shared cross-service code or contracts | `libs/` |
| Developer docs | `docs/dev/` |
| Documentation images or diagrams | `docs/assets/` |
| Repeatable setup scripts | `scripts/` |

## Adding a New Service

When adding a new service:

1. Create `apps/<service-name>/`.
2. Add source code, dependency manifest, Dockerfile, `.env.example`, and README.
3. Document service-specific commands in the service README.
4. Add the service to `infra/docker-compose.yml` if it should run in the local stack.
5. Add the service to the service index in this document.
6. Add quick commands to `docs/dev/CheatSheet.md` if developers need them often.

Keep detailed service behavior out of this document. The service README is the source of truth for that service.

## Related Docs

- [`README.md`](../../README.md) - user-facing project overview and basic run instructions.
- [`docs/dev/CheatSheet.md`](CheatSheet.md) - quick command reference.
- [`infra/README.md`](../../infra/README.md) - Docker Compose and infrastructure details.
- [`scripts/README.md`](../../scripts/README.md) - script usage.
- [`docs/dev/UsingKafka.md`](UsingKafka.md) - Kafka development notes.
