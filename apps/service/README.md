# Service

The `service` app is the Spring Boot service that listens for SherpaDB notifications and runs analytics workflows.

## Location

```text
apps/service
```

## Local Development

```bash
cd apps/service
./mvnw spring-boot:run
```

Local URL: `http://localhost:8080`

Docker Compose URL: `http://localhost:8083`

## Development Container

From the repository root:

```bash
docker compose -f infra/docker-compose.dev.yml up --build service
```

The development Compose service bind-mounts `apps/service` into `/app`. Spring Boot reload on `.java` file changes works through VS Code Java tooling. If you use another IDE, run `./mvnw compile` from `apps/service` or inside the container to trigger reload after Java source changes.

## Build

```bash
cd apps/service
./mvnw clean package
```

## Test

```bash
cd apps/service
./mvnw test
```
