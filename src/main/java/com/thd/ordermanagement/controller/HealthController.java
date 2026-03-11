package com.thd.ordermanagement.controller;

import com.thd.ordermanagement.dto.HealthResponse;
import com.thd.ordermanagement.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Service health check endpoint")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    @Operation(summary = "Health check", description = "Returns the health status of the service including database connectivity")
    @ApiResponse(responseCode = "200", description = "Health status retrieved successfully")
    public ResponseEntity<HealthResponse> getHealthStatus() {
        HealthResponse response = healthService.getHealthStatus();
        return ResponseEntity.ok(response);
    }
}
