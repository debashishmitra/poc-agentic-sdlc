package com.thd.ordermanagement.controller;

import com.thd.ordermanagement.dto.HealthResponse;
import com.thd.ordermanagement.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerHealthCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private HealthResponse healthResponse;

    @BeforeEach
    void setUp() {
        healthResponse = new HealthResponse(
                "UP",
                LocalDateTime.of(2025, 1, 15, 10, 30, 0),
                "order-management-service"
        );
    }

    @Test
    void testGetHealthStatus_Success() throws Exception {
        when(orderService.getHealthStatus()).thenReturn(healthResponse);

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.serviceName", is("order-management-service")));

        verify(orderService, times(1)).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_ReturnsCorrectJsonStructure() throws Exception {
        when(orderService.getHealthStatus()).thenReturn(healthResponse);

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.serviceName").exists());

        verify(orderService, times(1)).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_ReturnsStatusUp() throws Exception {
        when(orderService.getHealthStatus()).thenReturn(healthResponse);

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));

        verify(orderService, times(1)).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_ReturnsCorrectServiceName() throws Exception {
        when(orderService.getHealthStatus()).thenReturn(healthResponse);

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName", is("order-management-service")));

        verify(orderService, times(1)).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_ServiceThrowsRuntimeException_Returns500() throws Exception {
        when(orderService.getHealthStatus()).thenThrow(new RuntimeException("Unexpected service error"));

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/v1/orders/health"))
                .andExpect(status().isMethodNotAllowed());

        verify(orderService, never()).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_PutMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/api/v1/orders/health"))
                .andExpect(status().isMethodNotAllowed());

        verify(orderService, never()).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_DeleteMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/health"))
                .andExpect(status().isMethodNotAllowed());

        verify(orderService, never()).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_ReturnsTimestamp() throws Exception {
        when(orderService.getHealthStatus()).thenReturn(healthResponse);

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(orderService, times(1)).getHealthStatus();
    }

    @Test
    void testGetHealthStatus_ServiceReturnsNullStatus() throws Exception {
        HealthResponse nullStatusResponse = new HealthResponse(null, LocalDateTime.now(), "order-management-service");
        when(orderService.getHealthStatus()).thenReturn(nullStatusResponse);

        mockMvc.perform(get("/api/v1/orders/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName", is("order-management-service")));

        verify(orderService, times(1)).getHealthStatus();
    }
}