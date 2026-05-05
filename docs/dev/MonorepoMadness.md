# Monorepo Madness

This document explains where things live in the CloudSherpa repository and where new files should go. It is intentionally focused on directory. For service-specific setup, commands, ports, dependencies, and environment details, use the service READMEs under `apps/`.

!!! important "Do this first"

    After cloning the repository, configure Git to use the shared hook scripts before making commits:

    ```bash
    git config core.hooksPath scripts/hooks
    ```

## Branching Strategy

To maintain code quality and deployment stability, we strictly follow a main/dev/feature branch strategy:

* **[`main`](https://github.com/COS301-SE-2026/CloudSherpa/tree/main)**: This branch is strictly reserved for stable, production-ready code. Code is only merged into this branch after passing rigorous automated testing, Quality Assurance (QA), and User Acceptance Testing (UAT).
* **[`dev`](https://github.com/COS301-SE-2026/CloudSherpa/tree/dev)**: This is our active integration branch. Completed features are merged here first for testing and verification before being promoted to `main`.
* **`feature/*`**: Short-lived branches created from `dev` for individual features/fixes. Once complete and reviewed, they are merged back into `dev`.
* **`doc/*`**: Branches focused solely on documentation updates. Merged back into `dev` once complete.
* **`test/*`**: Branches created exclusively for adding or improving test coverage. Merged back into `dev` after passing CI checks.

## Repository Tree

_Generated on 26/04_. Should update regularly or automate via local git lifecycle hooks or github actions.

```text
.
|-- .env.example
|-- .gitignore
|-- README.md
|-- mkdocs.yml
|-- apps
|   |-- alert-engine
|   |-- dashboard
|   |-- ingestion
|   |-- intelligence
|   `-- service
|-- docs
|   |-- assets
|   |   `-- team-photos
|   |       |-- TeamLogo.png
|   |       `-- TeamPhoto.png
|   `-- dev
|       |-- CheatSheet.md
|       `-- MonorepoMadness.md
|-- infra
|   |-- README.md
|   `-- docker-compose.yml
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
- communicates with other services through explicit interfaces such as HTTP.

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
| Dashboard | [`docs/services/dashboard-frontend.md`](../services/dashboard-frontend.md) |
| Ingestion | [`docs/services/ingestion.md`](../services/ingestion.md) |
| Intelligence | [`docs/services/intelligence-engine.md`](../services/intelligence-engine.md) |
| Service | [`docs/services/service.md`](../services/service.md) |

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

### MkDocs

`mkdocs.yml` lives at the repository root because it configures the whole documentation site.

Use it for site navigation, theme, plugins, and build paths. Run `scripts/mkdocs-local.sh` from the repository root to serve the docs locally.

## `infra/`

Infrastructure files live in `infra/`.

Current files:

| File | Purpose |
| --- | --- |
| `infra/docker-compose.yml` | Local container stack for app services and shared dependencies. |
| `infra/README.md` | Infrastructure commands, ports, environment notes, and Docker Compose usage. |

Put container orchestration, local infrastructure wiring, and future deployment-adjacent configuration here. Do not put application source code in `infra/`.

## `libs/`

Shared cross-service contracts and reusable files belong in `libs/`.

Use `libs/` for:

- shared message contracts;
- shared validation rules;
- generated or shared types used by more than one service;
- cross-service utilities that are not owned by a single app.

Do not use `libs/` for service-specific business logic. If only one service uses it and owns it, keep it inside that service.

## `scripts/`

Developer scripts belong in `scripts/`.

Refer to `scripts/README.md`.

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
| Service-specific setup docs | `docs/services/<service-name>.md` symlinked to `apps/<service-name>/README.md` |
| Docker Compose or local stack wiring | `infra/` |
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

- `README.md` - user-facing project overview and basic run instructions.
- [`docs/dev/CheatSheet.md`](CheatSheet.md) - quick command reference.
- `infra/README.md` - Docker Compose and infrastructure details.
- `scripts/README.md` - script usage.
