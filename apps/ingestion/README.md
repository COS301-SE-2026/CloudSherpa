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
