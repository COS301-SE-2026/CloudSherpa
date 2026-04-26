import sys
import os
from confluent_kafka.cimpl import NewTopic
from confluent_kafka.admin import AdminClient

# Try env var, fall back to default
broker = os.getenv("KAFKA_BOOTSTRAP")
if broker == None:
    broker = "kafka:9092"

# Attempt to connect to kafka, if timeout, wait 2 seconds then retry
admin = AdminClient({
    "bootstrap.servers": broker,
    "retries": 5,
    "retry.backoff": 2000,
})

# CloudSherpa kafka topics are configured here
topics = [
    NewTopic("cloud-usage-events", num_partitions=1, replication_factor=1)
]

# Returns dict[str, concurrent.futures.Future] -> topics created asynchronously
fs = admin.create_topics(topics)

for topic, f in fs.items():
    try:
        # Calling f.result() blocks until Kafka responds with None if succesfull or throws an error on failure
        # Without calling f.result(), we do not know the outcome of the topic creation
        f.result()
        print(f"Created {topic}")
    except Exception as e:
        # f.result() throws a TOPIC_ALREADY_EXISTS error if the topic already exists on the broker, handle
        # gracefully. Else rethrow and fail.
        if "TOPIC_ALREADY_EXISTS" in str(e):
            print(f"Topic {topic} already exists")
        else:
            raise

# Exit cleanly to stop init service
sys.exit(0)