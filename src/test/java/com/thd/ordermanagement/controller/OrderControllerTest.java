package com.thd.ordermanagement.controller;

import com.thd.ordermanagement.dto.*;
import com.thd.ordermanagement.exception.InvalidOrderStateException;
import com.thd.ordermanagement.exception.OrderNotFoundException;
import com.thd.ordermanagement.model.OrderStatus;
import com.thd.ordermanagement.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;
    private UpdateOrderStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        OrderItemResponse itemResponse = new OrderItemResponse(
                1L,
                "SKU123",
                "Test Product",
                1,
                new BigDecimal("99.99")
        );

        testOrderResponse = new OrderResponse(
                1L,
                "John Doe",
                "john@example.com",
                "123 Main St",
                OrderStatus.PENDING,
                Arrays.asList(itemResponse),
                new BigDecimal("99.99"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductSku("SKU123");
        itemRequest.setProductName("Test Product");
        itemRequest.setQuantity(1);
        itemRequest.setUnitPrice(new BigDecimal("99.99"));

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerName("John Doe");
        createOrderRequest.setCustomerEmail("john@example.com");
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setItems(Arrays.asList(itemRequest));

        updateStatusRequest = new UpdateOrderStatusRequest();
        updateStatusRequest.setStatus(OrderStatus.CONFIRMED);
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerName", is("John Doe")))
                .andExpect(jsonPath("$.customerEmail", is("john@example.com")))
                .andExpect(jsonPath("$.shippingAddress", is("123 Main St")))
                .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                .andExpect(jsonPath("$.orderItems", hasSize(1)))
                .andExpect(jsonPath("$.totalAmount", is(99.99)));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void testCreateOrder_InvalidRequest_MissingCustomerName() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        invalidRequest.setCustomerEmail("john@example.com");
        invalidRequest.setShippingAddress("123 Main St");
        invalidRequest.setItems(Arrays.asList(new OrderItemRequest()));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void testCreateOrder_InvalidRequest_InvalidEmail() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        invalidRequest.setCustomerName("John Doe");
        invalidRequest.setCustomerEmail("invalid-email");
        invalidRequest.setShippingAddress("123 Main St");
        invalidRequest.setItems(Arrays.asList(new OrderItemRequest()));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void testCreateOrder_InvalidRequest_EmptyItems() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        invalidRequest.setCustomerName("John Doe");
        invalidRequest.setCustomerEmail("john@example.com");
        invalidRequest.setShippingAddress("123 Main St");
        invalidRequest.setItems(Arrays.asList());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void testGetAllOrders_Success() throws Exception {
        OrderItemResponse item2Response = new OrderItemResponse(
                2L,
                "SKU456",
                "Another Product",
                2,
                new BigDecimal("49.99")
        );

        OrderResponse order2Response = new OrderResponse(
                2L,
                "Jane Doe",
                "jane@example.com",
                "456 Oak St",
                OrderStatus.CONFIRMED,
                Arrays.asList(item2Response),
                new BigDecimal("99.98"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.getAllOrders()).thenReturn(Arrays.asList(testOrderResponse, order2Response));

        mockMvc.perform(get("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].customerName", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].customerName", is("Jane Doe")));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void testGetAllOrders_EmptyList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void testGetOrderById_Found() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(testOrderResponse);

        mockMvc.perform(get("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerName", is("John Doe")))
                .andExpect(jsonPath("$.customerEmail", is("john@example.com")))
                .andExpect(jsonPath("$.orderStatus", is("PENDING")));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new OrderNotFoundException("Order not found with id: 999"));

        mockMvc.perform(get("/api/v1/orders/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    void testGetOrdersByStatus_Pending() throws Exception {
        when(orderService.getOrdersByStatus(OrderStatus.PENDING))
                .thenReturn(Arrays.asList(testOrderResponse));

        mockMvc.perform(get("/api/v1/orders/status/PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderStatus", is("PENDING")));

        verify(orderService, times(1)).getOrdersByStatus(OrderStatus.PENDING);
    }

    @Test
    void testGetOrdersByStatus_Empty() throws Exception {
        when(orderService.getOrdersByStatus(OrderStatus.DELIVERED))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/orders/status/DELIVERED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService, times(1)).getOrdersByStatus(OrderStatus.DELIVERED);
    }

    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        OrderResponse updatedResponse = new OrderResponse(
                1L,
                "John Doe",
                "john@example.com",
                "123 Main St",
                OrderStatus.CONFIRMED,
                testOrderResponse.getOrderItems(),
                testOrderResponse.getTotalAmount(),
                testOrderResponse.getCreatedAt(),
                LocalDateTime.now()
        );

        when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderStatus", is("CONFIRMED")));

        verify(orderService, times(1)).updateOrderStatus(1L, OrderStatus.CONFIRMED);
    }

    @Test
    void testUpdateOrderStatus_InvalidTransition() throws Exception {
        when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                .thenThrow(new InvalidOrderStateException("Cannot change status of a cancelled order"));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).updateOrderStatus(1L, OrderStatus.CONFIRMED);
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() throws Exception {
        when(orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                .thenThrow(new OrderNotFoundException("Order not found with id: 999"));

        mockMvc.perform(patch("/api/v1/orders/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).updateOrderStatus(999L, OrderStatus.CONFIRMED);
    }

    @Test
    void testCancelOrder_Success() throws Exception {
        doNothing().when(orderService).cancelOrder(1L);

        mockMvc.perform(delete("/api/v1/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void testCancelOrder_AlreadyCancelled() throws Exception {
        doThrow(new InvalidOrderStateException("Order is already cancelled"))
                .when(orderService).cancelOrder(1L);

        mockMvc.perform(delete("/api/v1/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void testCancelOrder_AlreadyShipped() throws Exception {
        doThrow(new InvalidOrderStateException("Cannot cancel an order that has been shipped or delivered"))
                .when(orderService).cancelOrder(1L);

        mockMvc.perform(delete("/api/v1/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void testCancelOrder_OrderNotFound() throws Exception {
        doThrow(new OrderNotFoundException("Order not found with id: 999"))
                .when(orderService).cancelOrder(999L);

        mockMvc.perform(delete("/api/v1/orders/999/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).cancelOrder(999L);
    }
}
