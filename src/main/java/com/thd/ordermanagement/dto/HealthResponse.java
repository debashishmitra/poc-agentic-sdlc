package com.thd.ordermanagement.dto;

import com.thd.ordermanagement.model.HealthStatus;

import java.time.LocalDateTime;

public record HealthResponse(HealthStatus status, LocalDateTime timestamp, String serviceName) {
}
