# Normalization Service

Spring Boot service responsible for transforming raw ingested cloud data into normalized records for downstream analytics.

## Runtime

- Java 21
- Spring Boot 3.5
- Maven Wrapper
- Container port: `8080`
- Docker Compose host port: `8082`

## Local Setup

```bash
cd apps/normalization-service
chmod +x mvnw
```

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, this service should already have the `.env` file it needs for local development.

If the service needs Kafka locally, start the shared development dependencies and initialize topics from the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d --build kafka kafka-init schema-registry
```

Also start the ingestion service if events need to be produced either via the local server or docker container (local server preferred, containers built with production in mind, not optimized for dev currently)

Locally:
```bash
SERVER_PORT=8081 KAFKA_BOOTSTRAP_SERVERS=localhost:29092 SCHEMA_REGISTRY_URL=http://localhost:9000 ./apps/ingestion-service/mvnw spring-boot:run
```

Docker:
```bash
docker compose -f infra/docker-compose.yml up -d --build ingestion-service
```

## Development Server

```bash
./mvnw spring-boot:run
```

Spring Boot runs on port `8080` by default unless `SERVER_PORT` or application configuration overrides it.

If this service is run on the host and connects to the local Docker Compose Kafka stack, use host addresses:

```bash
SERVER_PORT=8082 KAFKA_BOOTSTRAP_SERVERS=localhost:29092 SCHEMA_REGISTRY_URL=http://localhost:9000 ./mvnw spring-boot:run
```

## Kafka Schema Workflow

Kafka message contracts are Avro schemas (`.avsc` files). The canonical shared schemas live in `libs/kafka/schemas`.

When this service produces or consumes Avro Kafka records, copy the required schemas from `libs/kafka/schemas` into `apps/normalization-service/src/main/avro` before building or running the Spring Boot app:

```bash
mkdir -p src/main/avro
cp ../../libs/kafka/schemas/*.avsc src/main/avro/
```

The copy step is expected to be automated later. Until then, update `libs/kafka/schemas` first, then refresh the service-local `src/main/avro` copy for every Spring Boot producer or consumer that uses the schema.

## Build and Production Run

```bash
./mvnw clean package
java -jar target/*.jar
```

To skip tests during a local packaging pass:

```bash
./mvnw clean package -DskipTests
```

## Tests

```bash
./mvnw test
```

## Docker

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up --build normalization-service
```

## Dev Dependencies

Maven resolves development and test dependencies from `pom.xml`.

Key development dependencies include:

- Spring Boot test support: `spring-boot-starter-test`
- Kafka test support: `spring-kafka-test`
- Maven Wrapper files: `mvnw` and `.mvn/wrapper`

Do not commit generated Maven output from `target/`.

## Environment Configuration

Local environment values should live in `.env`. Keep committed defaults and documentation in `.env.example`. Use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run, the local `.env` file should be in place.
- Use `localhost:29092` for Kafka when the app runs on the host.
- Use `kafka:9092` for Kafka when the app runs inside Docker Compose.
- Use `http://localhost:9000` for Schema Registry on the host.
- Use `http://schema-registry:8081` for Schema Registry inside Docker Compose.
- Keep schema or topic changes aligned with files under `libs/kafka/schemas`.
