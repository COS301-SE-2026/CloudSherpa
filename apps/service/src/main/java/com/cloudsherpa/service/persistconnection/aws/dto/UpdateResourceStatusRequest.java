package com.cloudsherpa.service.persistconnection.aws.dto;

import com.cloudsherpa.lib.entities.StatusEnum;

public record UpdateResourceStatusRequest(StatusEnum status) {}
