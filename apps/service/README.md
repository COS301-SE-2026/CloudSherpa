# Service

The `service` app is the Spring Boot service that owns analytics-facing API and persistence code.

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
