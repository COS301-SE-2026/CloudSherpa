# Analytics Engine

Spring Boot service responsible for analytics workflows over normalized cost and usage data.

## Runtime

- Java 21
- Spring Boot 3.5
- Maven Wrapper
- Container port: `8080`
- Docker Compose host port: `8083`

## Local Setup

```bash
cd apps/analytics-engine
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
docker compose -f infra/docker-compose.yml up --build analytics-engine
```

## Dev Dependencies

Maven resolves development and test dependencies from `pom.xml`.

Key development dependencies include:

- Spring Boot test support: `spring-boot-starter-test`
- Kafka test support: `spring-kafka-test`
- Maven Wrapper files: `mvnw` and `.mvn/wrapper`

Do not commit generated Maven output from `target/`. (the `.gitignore` enforces this)

## Environment Configuration

Local environment values should live in `.env`. Keep committed defaults and documentation in `.env.example`. Use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run, the local `.env` file should be in place.
- Use `localhost:29092` for Kafka when the app runs on the host.
- Use `kafka:9092` for Kafka when the app runs inside Docker Compose.
- Keep development-only overrides out of `application.properties` unless they are safe defaults.
