# How services Produce and Consume events 

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
````

---

