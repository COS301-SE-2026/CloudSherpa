# CloudSherpa Scripts

This directory contains scripts intended for configuring the repo, installing dev dependencies and automating the CloudSherpa dev environments.

| Script Name        | Script Purpose                                      |
|--------------------|-----------------------------------------------------|
| **env-init.sh**      | Recursively finds and safely copies `.env.example` files to `.env` files. Each service depends on its own isolated environment variables as far as possible. Shared environment variables are configured in the root `.env`. Script behaviour and usage is further documented via comments made in the file itself. |
| **spring-init.sh**      | Script used to for initializing services that uses Spring Boot framework, namely the `analytics-engine`, `ingestion-service` and `normalization-service`. This script is not meant for repeated runs. It just captures configuration in a structured format that can be reused for additional Spring Boot services. |
| **dev-dependencies.sh**      | Work in progress. **DO NOT EXECUTE**|
| **mkdocs-local.sh**      | Creates Python venv, installs required mkdocs pip packages, serves mkdocs locally. Run from root directory. |

## Environment Initialization

Run this once from the `scripts/` directory to copy every `.env.example` file to a matching `.env` file:

```bash
cd scripts
chmod +x env-init.sh
./env-init.sh
```

The script is idempotent and skips `.env` files that already exist. Once it has been run, local app and Docker Compose commands should have the expected `.env` files in place.
