package com.cloudsherpa.service.resourcediscovery.dto;

import java.util.List;

public record ResourceDiscoveryDto(List<String> services, CloudCredentialsDto credentials) {}
