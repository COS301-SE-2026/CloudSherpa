package com.cloudsherpa.service.persistconnection.dto;

import com.cloudsherpa.lib.entities.AccountTypeEnum;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CloudAccountDetailsResponse(
    UUID id,
    String displayName,
    AccountTypeEnum accountType,
    String accountEmail,
    String ingestionPeriod,
    OffsetDateTime createdAt) {
}
