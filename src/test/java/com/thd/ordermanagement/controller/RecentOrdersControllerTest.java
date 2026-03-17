package com.thd.ordermanagement.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.thd.ordermanagement.dto.OrderItemResponse;
import com.thd.ordermanagement.dto.OrderResponse;
import com.thd.ordermanagement.dto.RecentOrdersResponse;
import com.thd.ordermanagement.model.OrderStatus;
import com.thd.ordermanagement.service.OrderService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
public class RecentOrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderResponse orderResponse1;
    private OrderResponse orderResponse2;
    private OrderResponse orderResponse3;

    @BeforeEach
    void setUp() {
        OrderItemResponse itemResponse1 = new OrderItemResponse(
                1L,
                "SKU001",
                "Product 1",
                2,
                new BigDecimal("50.00")
        );

        OrderItemResponse itemResponse2 = new OrderItemResponse(
                2L,
                "SKU002",
                "Product 2",
                1,
                new BigDecimal("75.00")
        );

        OrderItemResponse itemResponse3 = new OrderItemResponse(
                3L,
                "SKU003",
                "Product 3",
                3,
                new BigDecimal("25.00")
        );

        LocalDateTime now = LocalDateTime.now();

        orderResponse1 = new OrderResponse(
                3L,
                "Alice Smith",
                "alice@example.com",
                "789 Oak Ave",
                OrderStatus.PENDING,
                Arrays.asList(itemResponse3),
                new BigDecimal("75.00"),
                now.minusMinutes(1),
                now.minusMinutes(1)
        );

        orderResponse2 = new OrderResponse(
                2L,
                "Jane Doe",
                "jane@example.com",
                "456 Elm St",
                OrderStatus.CONFIRMED,
                Arrays.asList(itemResponse2),
                new BigDecimal("75.00"),
                now.minusMinutes(10),
                now.minusMinutes(10)
        );

        orderResponse3 = new OrderResponse(
                1L,
                "John Doe",
                "john@example.com",
                "123 Main St",
                OrderStatus.PENDING,
                Arrays.asList(itemResponse1),
                new BigDecimal("100.00"),
                now.minusMinutes(30),
                now.minusMinutes(30)
        );
    }

    @Test
    void testGetRecentOrders_DefaultLimit_Success() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2, orderResponse3);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 3);

        when(orderService.getRecentOrders(10)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.orders", hasSize(3)))
                .andExpect(jsonPath("$.orders[0].id", is(3)))
                .andExpect(jsonPath("$.orders[0].customerName", is("Alice Smith")))
                .andExpect(jsonPath("$.orders[1].id", is(2)))
                .andExpect(jsonPath("$.orders[1].customerName", is("Jane Doe")))
                .andExpect(jsonPath("$.orders[2].id", is(1)))
                .andExpect(jsonPath("$.orders[2].customerName", is("John Doe")));

        verify(orderService, times(1)).getRecentOrders(10);
    }

    @Test
    void testGetRecentOrders_CustomLimit_Success() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 2);

        when(orderService.getRecentOrders(2)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.orders", hasSize(2)))
                .andExpect(jsonPath("$.orders[0].id", is(3)))
                .andExpect(jsonPath("$.orders[1].id", is(2)));

        verify(orderService, times(1)).getRecentOrders(2);
    }

    @Test
    void testGetRecentOrders_LimitOf1_Success() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 1);

        when(orderService.getRecentOrders(1)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.orders", hasSize(1)))
                .andExpect(jsonPath("$.orders[0].id", is(3)))
                .andExpect(jsonPath("$.orders[0].customerName", is("Alice Smith")));

        verify(orderService, times(1)).getRecentOrders(1);
    }

    @Test
    void testGetRecentOrders_MaxLimit50_Success() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2, orderResponse3);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 3);

        when(orderService.getRecentOrders(50)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.orders", hasSize(3)));

        verify(orderService, times(1)).getRecentOrders(50);
    }

    @Test
    void testGetRecentOrders_EmptyResults_Success() throws Exception {
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(Collections.emptyList(), 0);

        when(orderService.getRecentOrders(10)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.orders", hasSize(0)));

        verify(orderService, times(1)).getRecentOrders(10);
    }

    @Test
    void testGetRecentOrders_EmptyResultsWithCustomLimit_Success() throws Exception {
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(Collections.emptyList(), 0);

        when(orderService.getRecentOrders(5)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.orders", hasSize(0)));

        verify(orderService, times(1)).getRecentOrders(5);
    }

    @Test
    void testGetRecentOrders_InvalidLimit_Zero() throws Exception {
        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getRecentOrders(anyInt());
    }

    @Test
    void testGetRecentOrders_InvalidLimit_Negative() throws Exception {
        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getRecentOrders(anyInt());
    }

    @Test
    void testGetRecentOrders_InvalidLimit_ExceedsMax() throws Exception {
        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "51")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getRecentOrders(anyInt());
    }

    @Test
    void testGetRecentOrders_InvalidLimit_LargeNumber() throws Exception {
        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getRecentOrders(anyInt());
    }

    @Test
    void testGetRecentOrders_InvalidLimit_NotANumber() throws Exception {
        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getRecentOrders(anyInt());
    }

    @Test
    void testGetRecentOrders_VerifyJsonStructure() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 1);

        when(orderService.getRecentOrders(10)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.orders", hasSize(1)))
                .andExpect(jsonPath("$.orders[0].id", is(3)))
                .andExpect(jsonPath("$.orders[0].customerName", is("Alice Smith")))
                .andExpect(jsonPath("$.orders[0].customerEmail", is("alice@example.com")))
                .andExpect(jsonPath("$.orders[0].shippingAddress", is("789 Oak Ave")))
                .andExpect(jsonPath("$.orders[0].orderStatus", is("PENDING")))
                .andExpect(jsonPath("$.orders[0].orderItems", hasSize(1)))
                .andExpect(jsonPath("$.orders[0].orderItems[0].productSku", is("SKU003")))
                .andExpect(jsonPath("$.orders[0].orderItems[0].productName", is("Product 3")))
                .andExpect(jsonPath("$.orders[0].orderItems[0].quantity", is(3)))
                .andExpect(jsonPath("$.orders[0].orderItems[0].unitPrice", is(25.00)))
                .andExpect(jsonPath("$.orders[0].totalAmount", is(75.00)));

        verify(orderService, times(1)).getRecentOrders(10);
    }

    @Test
    void testGetRecentOrders_VerifySortOrder_NewestFirst() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2, orderResponse3);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 3);

        when(orderService.getRecentOrders(10)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id", is(3)))
                .andExpect(jsonPath("$.orders[1].id", is(2)))
                .andExpect(jsonPath("$.orders[2].id", is(1)));

        verify(orderService, times(1)).getRecentOrders(10);
    }

    @Test
    void testGetRecentOrders_MultipleOrderStatuses() throws Exception {
        OrderItemResponse itemResponse = new OrderItemResponse(
                4L,
                "SKU004",
                "Product 4",
                1,
                new BigDecimal("200.00")
        );

        LocalDateTime now = LocalDateTime.now();

        OrderResponse shippedOrder = new OrderResponse(
                4L,
                "Bob Johnson",
                "bob@example.com",
                "321 Pine Rd",
                OrderStatus.SHIPPED,
                Arrays.asList(itemResponse),
                new BigDecimal("200.00"),
                now,
                now
        );

        List<OrderResponse> orders = Arrays.asList(shippedOrder, orderResponse1, orderResponse2);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 3);

        when(orderService.getRecentOrders(3)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.orders", hasSize(3)))
                .andExpect(jsonPath("$.orders[0].orderStatus", is("SHIPPED")))
                .andExpect(jsonPath("$.orders[1].orderStatus", is("PENDING")))
                .andExpect(jsonPath("$.orders[2].orderStatus", is("CONFIRMED")));

        verify(orderService, times(1)).getRecentOrders(3);
    }

    @Test
    void testGetRecentOrders_ServiceThrowsRuntimeException() throws Exception {
        when(orderService.getRecentOrders(10)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/orders/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).getRecentOrders(10);
    }

    @Test
    void testGetRecentOrders_CountMatchesOrderListSize() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2);
        RecentOrdersResponse recentOrdersResponse = new RecentOrdersResponse(orders, 2);

        when(orderService.getRecentOrders(5)).thenReturn(recentOrdersResponse);

        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.orders", hasSize(2)));

        verify(orderService, times(1)).getRecentOrders(5);
    }

    @Test
    void testGetRecentOrders_InvalidLimit_DecimalNumber() throws Exception {
        mockMvc.perform(get("/api/v1/orders/recent")
                        .param("limit", "5.5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getRecentOrders(anyInt());
    }
}