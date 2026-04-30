package com.cloudsherpa.analytics.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.cloudsherpa.events.SherpaNormalizedEvent;

@Service
public class AnalyticsConsumer {
    @KafkaListener(topics = "${app.kafka.topics.sherpa-normalized}")
    private void consume(SherpaNormalizedEvent event) {
        System.out.println(event);
    }
}
