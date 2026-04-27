# Ingestion Service

Spring Boot service responsible for collecting raw cloud provider usage and billing data and introducing it into the CloudSherpa pipeline.

## Runtime

- Java 21
- Spring Boot 3.5
- Maven Wrapper
- Container port: `8080`
- Docker Compose host port: `8081`

## Local Setup

```bash
cd apps/ingestion-service
chmod +x mvnw
```

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, this service should already have the `.env` file it needs for local development.

If the service needs Kafka locally, start the shared development dependencies and initialize topics from the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d --build kafka kafka-init schema-registry
```

## Development Server

```bash
./mvnw spring-boot:run
```

Spring Boot runs on port `8080` by default unless `SERVER_PORT` or application configuration overrides it.

An override of environment variables for host-based dev might be:

```bash
SERVER_PORT=8081 KAFKA_BOOTSTRAP_SERVERS=localhost:29092 SCHEMA_REGISTRY_URL=http://localhost:9000 ./mvnw spring-boot:run
```

- Sets the local HTTP port with `SERVER_PORT`.
- Uses the host-published Kafka broker at `localhost:29092`.
- Uses the host-published Schema Registry at `http://localhost:9000`.

## Kafka Schema Workflow

Kafka message contracts are Avro schemas (`.avsc` files). The canonical shared schemas live in `libs/kafka/schemas`.

This Spring Boot service uses Avro-generated Java classes, so schemas must also be present in `apps/ingestion-service/src/main/avro` before Maven builds or runs the app. For now, copy the required schema files manually from the shared directory:

```bash
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
docker compose -f infra/docker-compose.yml up --build ingestion-service
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
- Keep provider sandbox credentials separate from production cloud credentials.
