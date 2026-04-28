package com.cloudsherpa.normalization.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.cloudsherpa.events.CloudUsageEvent;

@Service
public class NormalizationConsumer {
    /*
    * Decorator says:
    * Subscribe (attach/listen) to this topic
    * When a message arrives
    * - Deserialize it (according to Avro schema)
    * - Call this method as a callback with the deserialized CloudUsageEvent as a parameter
    * 
    * application.properties define behaviour, Spring Boot configures for us in the absence of
    * custom Kafka Consumer @Config class. We should try and achieve as much as possible via configuring
    * properties and let Spring Boot do the grunt work before we consider implementing our own consumer config,
    * but the option is available
    */
    @KafkaListener(topics = "${app.kafka.topics.cloud-usage}")
    void consume(CloudUsageEvent event) {
        System.out.println(event);
    }
}
