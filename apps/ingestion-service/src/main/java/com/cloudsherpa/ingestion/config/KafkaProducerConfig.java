package com.cloudsherpa.ingestion.config;

import com.cloudsherpa.events.CloudUsageEvent;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;


import java.util.Map;
import java.util.HashMap;

/**
 * Comments on Spring Boot Config class since first time working with it, hopefully this can be useful
 * for others as well
 * 
 * The @Configuration decorator
 * - This class defines objects (beans) that Spring should create and manage
 * 
 * What are Beans?
 * - A bean is an object managed by Spring
 * - Create once, reuse everywhere
 * 
 * The @Value decorator
 * - Inject values from ./resources/application.properties
 */

/**
 * This config answers the questions:
 * Where is Kafka?
 * How do we serialize messages (producer)?
 * Where is the schema registry?
 */

@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    // Build the Kafka producer config map
    @Bean
    public ProducerFactory<String, CloudUsageEvent> producerFactory() {
        // config = raw kafka settings
        Map<String, Object> config = new HashMap<>();

        // Connect to kafka at bootsrap server location, i.e. kafka:9092 or localhost:29092
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Kafka only understands bytes, everything must be serialized, convert config key to 
        // bytes using StringSerializer
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Facilitate Java -> Avro -> Binary -> Kafka
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        // Where to fetch/store schemas
        config.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(config);
    } 

    // What will be used to send events with kafkaTemplate.send
    @Bean
    public KafkaTemplate<String, CloudUsageEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
