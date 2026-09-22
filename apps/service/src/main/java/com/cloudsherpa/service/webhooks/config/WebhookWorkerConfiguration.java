package com.cloudsherpa.service.webhooks.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class WebhookWorkerConfiguration {
  // Named bean to distinguish delivery worker pool from other thread pools
  @Bean("webhookDeliveryExecutor")
  public ThreadPoolTaskExecutor webhookDeliveryExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("webhook-delivery-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    return executor;
  }
}
