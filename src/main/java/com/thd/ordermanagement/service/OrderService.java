package com.thd.ordermanagement.service;

import com.thd.ordermanagement.dto.CreateOrderRequest;
import com.thd.ordermanagement.dto.OrderCountSummaryResponse;
import com.thd.ordermanagement.dto.OrderResponse;
import com.thd.ordermanagement.dto.RecentOrdersResponse;
import com.thd.ordermanagement.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    List<OrderResponse> getOrdersByCustomerEmail(String email);

    List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    void cancelOrder(Long id);

    RecentOrdersResponse getRecentOrders(int limit);
}