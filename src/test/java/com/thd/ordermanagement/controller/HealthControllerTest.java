package com.thd.ordermanagement.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.thd.ordermanagement.dto.HealthResponse;
import com.thd.ordermanagement.model.HealthStatus;
import com.thd.ordermanagement.service.HealthService;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @Test
    void healthCheck_ShouldReturnCorrectStructure() throws Exception {
        HealthResponse response = new HealthResponse(
                HealthStatus.UP,
                LocalDateTime.of(2025, 1, 15, 10, 30, 0),
                "order-management-service"
        );
        when(healthService.getHealthStatus()).thenReturn(response);

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.serviceName", is("order-management-service")));

        verify(healthService, times(1)).getHealthStatus();
    }

    @Test
    void healthCheck_ShouldReturnDown_WhenServiceIsUnhealthy() throws Exception {
        HealthResponse response = new HealthResponse(
                HealthStatus.DOWN,
                LocalDateTime.now(),
                "order-management-service"
        );
        when(healthService.getHealthStatus()).thenReturn(response);

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DOWN")))
                .andExpect(jsonPath("$.serviceName", is("order-management-service")));

        verify(healthService, times(1)).getHealthStatus();
    }

    @Test
    void healthCheck_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/health"))
                .andExpect(status().isMethodNotAllowed());

        verify(healthService, never()).getHealthStatus();
    }

    @Test
    void healthCheck_PutMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/api/health"))
                .andExpect(status().isMethodNotAllowed());

        verify(healthService, never()).getHealthStatus();
    }

    @Test
    void healthCheck_DeleteMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/health"))
                .andExpect(status().isMethodNotAllowed());

        verify(healthService, never()).getHealthStatus();
    }

    @Test
    void healthCheck_ServiceThrowsException_Returns500() throws Exception {
        when(healthService.getHealthStatus()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isInternalServerError());

        verify(healthService, times(1)).getHealthStatus();
    }
}
