package com.cloudsherpa.ingestion.service;


import com.cloudsherpa.events.CloudUsageEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Intermediary between the CloudUsageController and Kafka. Currently this exposes a method to produce a mock
 * record in the cloud usage topic.
 */
@Service
public class CloudUsageService {
    
    // Use KafkaTemplate Bean defined in ../config/KafkaProducerConfig.java to interact with the cloud-usage-event kafka topic
    private final KafkaTemplate<String, CloudUsageEvent> kafkaTemplate;
    private final String topic;

    // Spring handles instantiation, we defined KafkaTemplate bean in config and we inject topic value from app properties
    public CloudUsageService(
        KafkaTemplate<String, CloudUsageEvent> kafkaTemplate,
        @Value("{app.kafka.topics.cloud-usage}")
        String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    // Temporary method used to test Kafka producer
    public void sendMockEvent() {

        // Build event according to schema for topic cloud-usage-event
        CloudUsageEvent event = CloudUsageEvent.newBuilder()
            .setProvider("AWS")
            .setAccountId("123")
            .setServiceName("EC2")
            .setUsageAmount(42.0)
            .setCost(12.75)
            .setCurrency("USD")
            .setTimestamp(System.currentTimeMillis())
            .build();
        
        // the template send method takes 3 args, the topic (like cloud-usage-event), the record key (account ID in this case)
        // and the record (event build from CloudUsageEvent schema defined in apps/ingestion-service/src/main/avro/cloud_usage_event.avsc)
        kafkaTemplate.send(topic, event.getAccountId().toString(), event);

        System.out.println("Produced record: " + event);
    }
}
