# Service

The `service` app is the Spring Boot service that listens for SherpaDB notifications and runs analytics workflows.

## Location

```text
apps/service
```

## Local Development

```bash
./mvnw -f apps/lib/pom.xml install -DskipTests
./mvnw -f apps/service/pom.xml spring-boot:run
```

Local URL: `http://localhost:8080`

Docker Compose URL: `http://localhost:8083`

## Development Container

From the repository root:

```bash
docker compose -f infra/docker-compose.dev.yml up --build service
```

The development Compose service bind-mounts the repository into `/app`. Spring Boot reload on `.java` file changes works through VS Code Java tooling. If you use another IDE, run `./mvnw -pl apps/service -am compile` from the repository root or inside the container to trigger reload after Java source changes.

## Build

```bash
./mvnw -pl apps/service -am clean package
```

## Test

```bash
./mvnw -pl apps/service -am test
```
