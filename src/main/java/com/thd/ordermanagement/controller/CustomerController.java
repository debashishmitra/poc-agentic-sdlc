package com.thd.ordermanagement.controller;

import com.thd.ordermanagement.dto.CustomerOrderSummaryDto;
import com.thd.ordermanagement.dto.OrderResponse;
import com.thd.ordermanagement.model.Order;
import com.thd.ordermanagement.service.CustomerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer Order API")
public class CustomerController {

    private final CustomerOrderService customerOrderService;

    @Autowired
    public CustomerController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping("/{email}/orders")
    @Operation(summary = "Get customer orders",
            description = "Retrieves paginated orders for a customer with optional date range filtering")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No orders found for customer")
    public ResponseEntity<Page<Order>> getCustomerOrders(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Order> orders = customerOrderService.getCustomerOrders(email, fromDate, toDate, pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{email}/summary")
    @Operation(summary = "Get customer order summary",
            description = "Retrieves aggregated order statistics for a customer")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No orders found for customer")
    public ResponseEntity<CustomerOrderSummaryDto> getCustomerOrderSummary(@PathVariable String email) {
        CustomerOrderSummaryDto summary = customerOrderService.getCustomerOrderSummary(email);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    @GetMapping("/{email}/exists")
    @Operation(summary = "Check if customer has orders",
            description = "Returns whether a customer has any orders in the system")
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    public ResponseEntity<Boolean> hasCustomerOrders(@PathVariable String email) {
        boolean hasOrders = customerOrderService.hasCustomerOrders(email);
        return new ResponseEntity<>(hasOrders, HttpStatus.OK);
    }
}
