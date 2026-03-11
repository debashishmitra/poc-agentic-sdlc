package com.thd.ordermanagement.service;

import com.thd.ordermanagement.dto.HealthResponse;
import com.thd.ordermanagement.model.HealthStatus;
import com.thd.ordermanagement.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);
    private static final String SERVICE_NAME = "order-management-service";

    private final OrderRepository orderRepository;

    public HealthService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Performs a health check by verifying database connectivity.
     *
     * @return health status including DB availability
     */
    public HealthResponse getHealthStatus() {
        try {
            orderRepository.count();
            return new HealthResponse(HealthStatus.UP, LocalDateTime.now(), SERVICE_NAME);
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            return new HealthResponse(HealthStatus.DOWN, LocalDateTime.now(), SERVICE_NAME);
        }
    }
}
