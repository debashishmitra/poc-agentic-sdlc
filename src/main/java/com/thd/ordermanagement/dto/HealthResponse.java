package com.thd.ordermanagement.dto;

import java.time.LocalDateTime;

public class HealthResponse {

    private String status;
    private LocalDateTime timestamp;
    private String serviceName;

    public HealthResponse() {
    }

    public HealthResponse(String status, LocalDateTime timestamp, String serviceName) {
        this.status = status;
        this.timestamp = timestamp;
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}