package com.thd.ordermanagement.service;

import com.thd.ordermanagement.dto.HealthResponse;
import com.thd.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplHealthTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        // No special setup needed for health check tests
    }

    @Test
    void should_returnHealthResponse_when_serviceIsRunning() {
        // Arrange
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertEquals("order-management-service", response.getServiceName());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void should_returnStatusUp_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertEquals("UP", response.getStatus());
    }

    @Test
    void should_returnCorrectServiceName_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertEquals("order-management-service", response.getServiceName());
    }

    @Test
    void should_returnCurrentTimestamp_when_healthCheckInvoked() {
        // Arrange
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        LocalDateTime afterCall = LocalDateTime.now();
        assertNotNull(response.getTimestamp());
        assertFalse(response.getTimestamp().isBefore(beforeCall),
                "Timestamp should not be before the method was called");
        assertFalse(response.getTimestamp().isAfter(afterCall),
                "Timestamp should not be after the method returned");
    }

    @Test
    void should_returnNonNullResponse_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertNotNull(response.getTimestamp());
        assertNotNull(response.getServiceName());
    }

    @Test
    void should_returnConsistentResults_when_calledMultipleTimes() {
        // Arrange - no setup needed

        // Act
        HealthResponse response1 = orderService.getHealthStatus();
        HealthResponse response2 = orderService.getHealthStatus();

        // Assert
        assertEquals(response1.getStatus(), response2.getStatus());
        assertEquals(response1.getServiceName(), response2.getServiceName());
        assertNotNull(response1.getTimestamp());
        assertNotNull(response2.getTimestamp());
        // Second call should have same or later timestamp
        assertFalse(response2.getTimestamp().isBefore(response1.getTimestamp()),
                "Second call timestamp should not be before first call timestamp");
    }

    @Test
    void should_notInteractWithRepository_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertNotNull(response);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void should_returnStatusAsString_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertInstanceOf(String.class, response.getStatus());
        assertFalse(response.getStatus().isEmpty(), "Status should not be empty");
    }

    @Test
    void should_returnServiceNameAsString_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertInstanceOf(String.class, response.getServiceName());
        assertFalse(response.getServiceName().isEmpty(), "Service name should not be empty");
    }

    @Test
    void should_returnTimestampAsLocalDateTime_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert
        assertInstanceOf(LocalDateTime.class, response.getTimestamp());
    }

    @Test
    void should_returnAllFieldsPopulated_when_healthCheckInvoked() {
        // Arrange - no setup needed

        // Act
        HealthResponse response = orderService.getHealthStatus();

        // Assert - verify none of the fields are null or empty strings
        assertNotNull(response.getStatus(), "Status must not be null");
        assertNotNull(response.getTimestamp(), "Timestamp must not be null");
        assertNotNull(response.getServiceName(), "Service name must not be null");
        assertNotEquals("", response.getStatus().trim(), "Status must not be blank");
        assertNotEquals("", response.getServiceName().trim(), "Service name must not be blank");
    }

    private void verifyNoInteractions(OrderRepository repository) {
        org.mockito.Mockito.verifyNoInteractions(repository);
    }
}