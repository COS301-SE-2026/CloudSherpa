package com.cloudsherpa.normalization.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cloudsherpa.events.CloudUsageEvent;
import com.cloudsherpa.events.SherpaNormalizedEvent;

@Service
public class NormalizationProducer {
    private final KafkaTemplate<String, SherpaNormalizedEvent> kafkaTemplate;
    private final String topic;

    public NormalizationProducer(
        KafkaTemplate<String, SherpaNormalizedEvent> kafkaTemplate,
        @Value("${app.kafka.topics.sherpa-normalized}")
        String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendNormalizedEvent(CloudUsageEvent event) {
        SherpaNormalizedEvent normalizedEvent = SherpaNormalizedEvent.newBuilder()
            .setProvider(event.getProvider())
            .setAccountId(event.getAccountId())
            .setCreatedAt(Instant.ofEpochMilli(event.getTimestamp()))
            .setEnvironmentId(event.getServiceName())
            .setRecordedAt(event.getTimestamp())
            .setResourceId("Stub Resource ID")
            .setServiceCategory("Stub service category")
            .setUsageUnit("Stub Usage Unit")
            .setUsageAmount(event.getUsageAmount())
            .setCurrency(event.getCurrency())
            .setCostAmount(event.getCost())
            .build();
        
        kafkaTemplate.send(topic, normalizedEvent.getAccountId().toString(), normalizedEvent);
    }

    
}
