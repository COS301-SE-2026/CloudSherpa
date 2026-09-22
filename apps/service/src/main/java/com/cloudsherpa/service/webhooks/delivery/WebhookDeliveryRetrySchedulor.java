package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryRetrySchedulor {
  private final UserRepository userRepository;
  private final WebhookDeliveryRetryService retryService;

  public WebhookDeliveryRetrySchedulor(
      UserRepository userRepository, WebhookDeliveryRetryService retryService) {
    this.userRepository = userRepository;
    this.retryService = retryService;
  }

  @Scheduled(fixedDelay = 10_00)
  public void retryFailedDeliveries() {
    for (User user : userRepository.findAll()) {
      retryService.retryFailedDeliveries(user.getId());
    }
  }
}
