package com.cloudsherpa.lib.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.Webhook;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {}
