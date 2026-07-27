package com.cloudsherpa.ingestion.controller.dto;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import java.util.List;

public record ResourceDiscoveryRequest(List<String> services, CloudCredentials credentials) {}
