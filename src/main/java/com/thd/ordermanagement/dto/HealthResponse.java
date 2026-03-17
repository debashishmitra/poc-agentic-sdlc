package com.thd.ordermanagement.dto;

import java.time.LocalDateTime;

import com.thd.ordermanagement.model.HealthStatus;

public record HealthResponse(HealthStatus status, LocalDateTime timestamp, String serviceName) {
}
