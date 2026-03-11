package com.thd.ordermanagement.service;

import com.thd.ordermanagement.dto.HealthResponse;
import com.thd.ordermanagement.model.HealthStatus;
import com.thd.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private HealthService healthService;

    @Test
    void should_returnUp_when_databaseIsAccessible() {
        when(orderRepository.count()).thenReturn(5L);

        LocalDateTime before = LocalDateTime.now();
        HealthResponse response = healthService.getHealthStatus();
        LocalDateTime after = LocalDateTime.now();

        assertEquals(HealthStatus.UP, response.status());
        assertEquals("order-management-service", response.serviceName());
        assertNotNull(response.timestamp());
        assertFalse(response.timestamp().isBefore(before));
        assertFalse(response.timestamp().isAfter(after));
    }

    @Test
    void should_returnDown_when_databaseIsUnreachable() {
        when(orderRepository.count()).thenThrow(new RuntimeException("Connection refused"));

        HealthResponse response = healthService.getHealthStatus();

        assertEquals(HealthStatus.DOWN, response.status());
        assertEquals("order-management-service", response.serviceName());
        assertNotNull(response.timestamp());
    }

    @Test
    void should_returnCorrectServiceName() {
        when(orderRepository.count()).thenReturn(0L);

        HealthResponse response = healthService.getHealthStatus();

        assertEquals("order-management-service", response.serviceName());
    }

    @Test
    void should_returnConsistentResults_when_calledMultipleTimes() {
        when(orderRepository.count()).thenReturn(10L);

        HealthResponse response1 = healthService.getHealthStatus();
        HealthResponse response2 = healthService.getHealthStatus();

        assertEquals(response1.status(), response2.status());
        assertEquals(response1.serviceName(), response2.serviceName());
        assertFalse(response2.timestamp().isBefore(response1.timestamp()));
    }
}
