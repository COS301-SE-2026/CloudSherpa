package com.cloudsherpa.service.persistconnection.provider.gcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GcpCredentialsDto(
    @JsonProperty("type") String type,
    @JsonProperty("project_id") String projectId,
    @JsonProperty("private_key_id") String privateKeyId,
    @JsonProperty("private_key") String privateKey,
    @JsonProperty("client_email") String clientEmail,
    @JsonProperty("client_id") String clientId,
    @JsonProperty("auth_uri") String authUri,
    @JsonProperty("token_uri") String tokenUri) {}
