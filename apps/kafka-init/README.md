# Kafka Init

One-shot Python container that initializes CloudSherpa Kafka topics for the local Docker Compose stack.

## Runtime

- Python 3.11
- `confluent-kafka`
- Runs once, creates topics, then exits
- No exposed HTTP port

## Local Setup

```bash
cd apps/kafka-init
python -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

The `python -m venv .venv` command depends on how Python is installed on your machine. If it fails, make sure the Python `venv` module is installed, then try `python3 -m venv .venv`.

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, this service should already have the `.env` file it needs for local development.

## Run Against Docker Kafka

Start Kafka first from the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d kafka
```

Then run the init container:

```bash
docker compose -f infra/docker-compose.yml up --build kafka-init
```

The service exits successfully after creating configured topics. Existing topics are skipped.

## Docker Compose

In the full stack, `kafka-init` runs after `kafka` starts:

```bash
docker compose -f infra/docker-compose.yml up --build
```

## Environment Configuration

Local environment values should live in `.env`. Keep committed defaults and documentation in `.env.example`. Use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run, the local `.env` file should be in place.
- Use `KAFKA_BOOTSTRAP=kafka:9092` when running inside Docker Compose.
- Use `KAFKA_BOOTSTRAP=localhost:29092` only when running the Python script directly on the host.
