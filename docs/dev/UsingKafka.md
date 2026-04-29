# How Services Produce and Consume Events

## Resources

Free video lecture series done by Confluent: [Apache Kafka 101](https://developer.confluent.io/courses/apache-kafka/events/)

A Kafka-based service usually interacts with Kafka in one of two ways:

- **Producer**: publishes events to a Kafka topic.
- **Consumer**: subscribes to a Kafka topic and reacts to events.

Example flow:

```text
Cloud Provider API
      |
      v
Ingestion Service  --->  cloud.raw.aws.usage
                              |
                              v
                     Normalization Service
                              |
                              v
                     cloud.normalized.usage
                              |
                              v
              Analytics / AI Services
```

## Schema Registry Workflow

Kafka message contracts are defined as Avro schema files (`.avsc`). The source of truth for shared Kafka schemas is:

```text
libs/kafka/schemas
```

When a schema changes:

1. Update or add the `.avsc` file under `libs/kafka/schemas`.
2. Copy the required `.avsc` files into each Spring Boot producer or consumer under `apps/<service>/src/main/avro`.
3. Rebuild the Spring Boot service so Maven can generate Avro classes from `src/main/avro`.
4. Run the affected producer and consumer against the same Schema Registry instance.

The copy from `libs/kafka/schemas` to `apps/<service>/src/main/avro` is manual for now and will be automated later. Do not treat a service-local `src/main/avro` file as the canonical schema if it differs from `libs/kafka/schemas`.

Example from a Spring Boot service directory:

```bash
mkdir -p src/main/avro
cp ../../libs/kafka/schemas/*.avsc src/main/avro/
./mvnw clean package
```

## Local Connection Values

Use the address that matches where the client process runs:

| Client location | Kafka bootstrap servers | Schema Registry URL |
| --- | --- | --- |
| Host machine | `localhost:29092` | `http://localhost:9000` |
| Docker Compose network | `kafka:9092` | `http://schema-registry:8081` |

Kafka bootstrap servers are broker addresses and should not include protocol (i.e. `http://`). Schema Registry is an HTTP service and should include the `http://` scheme.

## Topics naming convention

Defines CloudSherpa Kafka topics naming convention (used <https://www.confluent.io/learn/kafka-topic-naming-convention/#components-of-a-kafka-topic-name> for inspiration)

`<domain>.<data-type/action>.version`

Identified domains:
- `cloud` generic cloud domain
- `aws` topics specific to aws messages
- `gcp` topics specific to gcp messages
- `azure` topics specific to azure messages
- `sherpa` Messages generated and published by CloudSherpa services, like the normalization and alert-engine service.  

Data types are context specific, like `usage`, `performance` or `billing`.

Version represents the schema contract of the topic. It is incremented only when a breaking (non-backward-compatible) schema change is introduced (the scema in `apps/<service>/src/main/avro` is changed in a breaking way), a breaking schema change has cascading effects. Changing the version emphasises that producer/consumer logic needs to be revised according to new schema. 

!!! note
      Be sure to run either docker compose build or just add the `--build` flag when starting the `kafka-init` service after topics changed. Luckily very quick to rebuild once initially built.

## Reference Links
Links to Kafka, Confluent and other related docs and tutorials developers found useful

Quick reference/overview of sending/receiving messages: <https://docs.spring.io/spring-boot/reference/messaging/kafka.html#messaging.kafka>

### Producer (sending messages, `KafkaTemplate`)
- <https://docs.spring.io/spring-kafka/reference/kafka/sending-messages.html#kafka-template>
- <https://www.geeksforgeeks.org/java/spring-boot-kafka-producer-example/>

### Consumer (receiving messages, `@KafkaListener`)
- <https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html>
- <https://www.geeksforgeeks.org/java/spring-boot-kafka-consumer-example/>

### Schema Registry, Serialization & Deserialization
- <https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/index.html#configuration-details>
- <https://docs.spring.io/spring-kafka/reference/kafka/serdes.html>
