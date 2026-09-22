package com.cloudsherpa.service.auth.dto;

public record AuthSession(AuthUserResponse user, String accessToken, String refreshToken) {}
