package com.cloudsherpa.lib.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.WebhookDelivery;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {}
