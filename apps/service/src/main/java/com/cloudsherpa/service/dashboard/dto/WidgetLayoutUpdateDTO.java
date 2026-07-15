package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record WidgetLayoutUpdateDTO(UUID id, Integer x, Integer y, Integer w, Integer h) {}
