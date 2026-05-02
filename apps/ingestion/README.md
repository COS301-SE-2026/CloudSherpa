# Ingestion

The `ingestion` app is the Spring Boot service that owns cloud ingestion and normalization code.

## Location

```text
apps/ingestion
```

## Local Development

```bash
cd apps/ingestion
./mvnw spring-boot:run
```

Local URL: `http://localhost:8080`

Docker Compose URL: `http://localhost:8081`

## Development Container

From the repository root:

```bash
docker compose -f infra/docker-compose.dev.yml up --build ingestion
```

The development Compose service bind-mounts `apps/ingestion` into `/app`. Spring Boot reload on `.java` file changes works through VS Code Java tooling. If you use another IDE, run `./mvnw compile` from `apps/ingestion` or inside the container to trigger reload after Java source changes.

## Build

```bash
cd apps/ingestion
./mvnw clean package
```

## Test

```bash
cd apps/ingestion
./mvnw test
```
