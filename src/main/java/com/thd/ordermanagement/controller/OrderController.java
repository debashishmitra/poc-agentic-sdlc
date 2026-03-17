package com.thd.ordermanagement.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thd.ordermanagement.dto.CreateOrderRequest;
import com.thd.ordermanagement.dto.OrderCountSummaryResponse;
import com.thd.ordermanagement.dto.OrderResponse;
import com.thd.ordermanagement.dto.RecentOrdersResponse;
import com.thd.ordermanagement.dto.UpdateOrderStatusRequest;
import com.thd.ordermanagement.model.OrderStatus;
import com.thd.ordermanagement.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order Management API")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details and items")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves a list of all orders")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderService.getAllOrders();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID")
    @ApiResponse(responseCode = "200", description = "Order retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieves all orders with the specified status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> responses = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{email}")
    @Operation(summary = "Get orders by customer email", description = "Retrieves all orders for a specific customer email")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerEmail(@PathVariable String email) {
        List<OrderResponse> responses = orderService.getOrdersByCustomerEmail(email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get orders by date range", description = "Retrieves all orders created within the specified date range")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OrderResponse> responses = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponse(responseCode = "200", description = "Order status updated successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/counts")
    @Operation(summary = "Get order count summary", description = "Returns the count of orders grouped by each OrderStatus along with a total order count")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order count summary")
    public ResponseEntity<OrderCountSummaryResponse> getOrderCountSummary() {
        OrderCountSummaryResponse summary = orderService.getOrderCountSummary();
        return ResponseEntity.ok(summary);
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Cancels an existing order")
    @ApiResponse(responseCode = "204", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent orders", description = "Retrieves the N most recently created orders, sorted by creation date descending (newest first)")
    @ApiResponse(responseCode = "200", description = "Recent orders retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid limit parameter")
    public ResponseEntity<RecentOrdersResponse> getRecentOrders(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        logger.info("Received request to get recent orders with limit: {}", limit);
        RecentOrdersResponse response = orderService.getRecentOrders(limit);
        logger.info("Returning {} recent orders", response.getCount());
        return ResponseEntity.ok(response);
    }
}