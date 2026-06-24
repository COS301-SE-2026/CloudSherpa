# Ingestion

The `ingestion` app is the Spring Boot service that owns cloud ingestion and normalization code.

## Location

```text
apps/ingestion
```

## Local Development

```bash
./mvnw -f apps/lib/pom.xml install -DskipTests
./mvnw -f apps/ingestion/pom.xml spring-boot:run
```

Local URL: `http://localhost:8080`

Docker Compose URL: `http://localhost:8081`

## Development Container

From the repository root:

```bash
docker compose -f infra/docker-compose.dev.yml up --build ingestion
```

The development Compose service bind-mounts the repository into `/app`. Spring Boot reload on `.java` file changes works through VS Code Java tooling. If you use another IDE, run `./mvnw -pl apps/ingestion -am compile` from the repository root or inside the container to trigger reload after Java source changes.

## Build

```bash
./mvnw -pl apps/ingestion -am clean package
```

## Test

```bash
./mvnw -pl apps/ingestion -am test
```
