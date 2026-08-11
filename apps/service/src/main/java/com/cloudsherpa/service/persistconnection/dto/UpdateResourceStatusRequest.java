package com.cloudsherpa.service.persistconnection.dto;

import com.cloudsherpa.lib.entities.StatusEnum;

public record UpdateResourceStatusRequest(StatusEnum status) {}
