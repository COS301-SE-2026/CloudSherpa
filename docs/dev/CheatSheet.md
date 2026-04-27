# CloudSherpa Cheat Sheet

Quick commands for day-to-day development. Run commands from the repository root unless a section says otherwise.

## First-Time Setup

Initialize local `.env` files:

```bash
cd scripts
chmod +x env-init.sh
./env-init.sh
cd ..
```

Once `env-init.sh` has been run, the repo should have the local `.env` files needed by app and Docker Compose commands. The script is idempotent and skips `.env` files that already exist.

Install app dependencies as needed:

```bash
cd apps/dashboard-frontend && npm install
cd ../dashboard-backend && npm install
cd ../alert-engine && npm install
```

For Python:

```bash
cd apps/intelligence-engine
python -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

The `python -m venv .venv` command depends on your Python installation. If it fails, make sure the Python `venv` module is installed, then try `python3 -m venv .venv`.

Spring Boot services use their Maven wrappers:

```bash
chmod +x apps/ingestion-service/mvnw
chmod +x apps/normalization-service/mvnw
chmod +x apps/analytics-engine/mvnw
```

## Docker Compose

Start Kafka, initialize topics, and start Schema Registry:

```bash
docker compose -f infra/docker-compose.yml up -d --build kafka kafka-init schema-registry
```

Start AnalyticsDB (TimescaleDB):

```bash
docker compose -f infra/docker-compose.yml up -d analytics-db
```

Connect to AnalyticsDB:

```bash
docker exec -it analytics-db sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Start the full local stack:

```bash
docker compose -f infra/docker-compose.yml up --build
```

Start one service:

```bash
docker compose -f infra/docker-compose.yml up --build dashboard-frontend
```

Stop the stack:

```bash
docker compose -f infra/docker-compose.yml down
```

Follow logs:

```bash
docker compose -f infra/docker-compose.yml logs -f
docker compose -f infra/docker-compose.yml logs -f kafka
```

Check container status:

```bash
docker compose -f infra/docker-compose.yml ps
```

## Local Dev Servers

### Dashboard Frontend

```bash
cd apps/dashboard-frontend
npm run dev
```

URL: `http://localhost:3000`

### Dashboard Backend

```bash
cd apps/dashboard-backend
npm run start:dev
```

URL: `http://localhost:3001`

### Alert Engine

```bash
cd apps/alert-engine
npm run dev
```

URL: `http://localhost:3000` locally, or `http://localhost:3002` through Docker Compose.

### Intelligence Engine

```bash
cd apps/intelligence-engine
source .venv/bin/activate
uvicorn src.main:app --reload --host 0.0.0.0 --port 8000
```

URL: `http://localhost:8000`

### Ingestion Service

```bash
cd apps/ingestion-service
./mvnw spring-boot:run
```

URL: `http://localhost:8080` locally, or `http://localhost:8081` through Docker Compose.

### Normalization Service

```bash
cd apps/normalization-service
./mvnw spring-boot:run
```

URL: `http://localhost:8080` locally, or `http://localhost:8082` through Docker Compose.

### Analytics Engine

```bash
cd apps/analytics-engine
./mvnw spring-boot:run
```

URL: `http://localhost:8080` locally, or `http://localhost:8083` through Docker Compose.

## Build Commands

```bash
cd apps/dashboard-frontend && npm run build
cd apps/dashboard-backend && npm run build
cd apps/alert-engine && npm run build
cd apps/ingestion-service && ./mvnw clean package
cd apps/normalization-service && ./mvnw clean package
cd apps/analytics-engine && ./mvnw clean package
```

For Spring Boot builds without tests:

```bash
./mvnw clean package -DskipTests
```

## Test Commands

```bash
cd apps/dashboard-backend && npm test
cd apps/dashboard-backend && npm run test:e2e
cd apps/alert-engine && npm test
cd apps/ingestion-service && ./mvnw test
cd apps/normalization-service && ./mvnw test
cd apps/analytics-engine && ./mvnw test
```

## Lint and Format

```bash
cd apps/dashboard-frontend && npm run lint
cd apps/dashboard-backend && npm run lint
cd apps/dashboard-backend && npm run format
```

## Ports

| Service | Local Dev Port | Docker Compose Host Port |
| --- | ---: | ---: |
| Dashboard Frontend | `3000` | `3000` |
| Dashboard Backend | `3001` | `3001` |
| Alert Engine | `3000` | `3002` |
| Intelligence Engine | `8000` | `8000` |
| Kafka Init | n/a | n/a |
| Ingestion Service | `8080` | `8081` |
| Normalization Service | `8080` | `8082` |
| Analytics Engine | `8080` | `8083` |
| Schema Registry | n/a | `9000` |
| Kafka | n/a | `29092` |

## Kafka Addresses

- Host machine clients: `localhost:29092`
- Docker Compose clients: `kafka:9092`
- Host Schema Registry: `http://localhost:9000`
- Docker Compose Schema Registry: `http://schema-registry:8081`

## Useful Paths

| Path | Purpose |
| --- | --- |
| `apps/` | Runtime services |
| `infra/docker-compose.yml` | Local container stack |
| `libs/kafka/` | Shared Kafka schemas |
| `apps/kafka-init/` | Local Kafka topic initialization service |
| `scripts/env-init.sh` | Initializes local `.env` files |
| `docs/dev/MonorepoMadness.md` | Repo structure guide |
| `docs/dev/UsingKafka.md` | Kafka usage notes |

## Troubleshooting

Rebuild one Docker image without cache:

```bash
docker compose -f infra/docker-compose.yml build --no-cache dashboard-backend
```

Remove containers and anonymous volumes:

```bash
docker compose -f infra/docker-compose.yml down -v
```

If Kafka connections fail, check whether the client is running on the host or inside Docker Compose and use the matching Kafka address.
