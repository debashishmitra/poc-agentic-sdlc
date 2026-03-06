```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CustomerSearchRequest;
import com.example.ecommerce.dto.CustomerSearchResponse;
import com.example.ecommerce.dto.OrderHistoryResponse;
import com.example.ecommerce.service.CustomerService;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for customer search and order history operations.
 * Provides endpoints for searching customers and retrieving order history.
 * 
 * @author Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    /**
     * Search customers based on various criteria with pagination support.
     * 
     * @param searchRequest the search criteria containing filters
     * @param page the page number (0-based, default: 0)
     * @param size the page size (default: 20, max: 100)
     * @param sortBy the field to sort by (default: "id")
     * @param sortDir the sort direction (asc/desc, default: "asc")
     * @return paginated list of customers matching the search criteria
     */
    @PostMapping("/search")
    public ResponseEntity<Page<CustomerSearchResponse>> searchCustomers(
            @Valid @RequestBody CustomerSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Searching customers with criteria: {}, page: {}, size: {}", 
                searchRequest, page, size);
        
        try {
            // Create sort direction
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Perform search
            Page<CustomerSearchResponse> customers = customerService.searchCustomers(searchRequest, pageable);
            
            log.info("Found {} customers matching search criteria", customers.getTotalElements());
            
            return ResponseEntity.ok(customers);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error searching customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get customer details by ID.
     * 
     * @param customerId the customer ID
     * @return customer details
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerSearchResponse> getCustomerById(
            @PathVariable @NotNull @Positive Long customerId) {
        
        log.info("Retrieving customer details for ID: {}", customerId);
        
        try {
            CustomerSearchResponse customer = customerService.getCustomerById(customerId);
            
            if (customer == null) {
                log.warn("Customer not found with ID: {}", customerId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("Successfully retrieved customer details for ID: {}", customerId);
            return ResponseEntity.ok(customer);
            
        } catch (Exception e) {
            log.error("Error retrieving customer with ID {}: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get order history for a specific customer with pagination and filtering.
     * 
     * @param customerId the customer ID
     * @param page the page number (0-based, default: 0)
     * @param size the page size (default: 20, max: 100)
     * @param sortBy the field to sort by (default: "orderDate")
     * @param sortDir the sort direction (asc/desc, default: "desc")
     * @param status optional order status filter
     * @param fromDate optional start date filter (ISO format)
     * @param toDate optional end date filter (ISO format)
     * @return paginated list of customer orders
     */
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<Page<OrderHistoryResponse>> getCustomerOrderHistory(
            @PathVariable @NotNull @Positive Long customerId,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        log.info("Retrieving order history for customer ID: {}, page: {}, size: {}, status: {}", 
                customerId, page, size, status);
        
        try {
            // Verify customer exists
            if (!customerService.customerExists(customerId)) {
                log.warn("Customer not found with ID: {}", customerId);
                return ResponseEntity.notFound().build();
            }
            
            // Create sort direction
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Parse date filters if provided
            LocalDateTime fromDateTime = null;
            LocalDateTime toDateTime = null;
            
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                fromDateTime = LocalDateTime.parse(fromDate);
            }
            
            if (toDate != null && !toDate.trim().isEmpty()) {
                toDateTime = LocalDateTime.parse(toDate);
            }
            
            // Get order history
            Page<OrderHistoryResponse> orderHistory = orderService.getCustomerOrderHistory(
                    customerId, status, fromDateTime, toDateTime, pageable);
            
            log.info("Found {} orders for customer ID: {}", orderHistory.getTotalElements(), customerId);
            
            return ResponseEntity.ok(orderHistory);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid parameters for order history request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving order history for customer {}: {}", 
                    customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get recent orders for a customer (last 10 orders).
     * 
     * @param customerId the customer ID
     * @return list of recent orders
     */
    @GetMapping("/{customerId}/orders/recent")
    public ResponseEntity<List<OrderHistoryResponse>> getRecentOrders(
            @PathVariable @NotNull @Positive Long customerId) {
        
        log.info("Retrieving recent orders for customer ID: {}", customerId);
        
        try {
            // Verify customer exists
            if (!customerService.customerExists(customerId)) {
                log.warn("Customer not found with ID: {}", customerId);
                return ResponseEntity.notFound().build();
            }
            
            List<OrderHistoryResponse> recentOrders = orderService.getRecentOrdersByCustomer(customerId, 10);
            
            log.info("Found {} recent orders for customer ID: {}", recentOrders.size(), customerId);
            
            return ResponseEntity.ok(recentOrders);
            
        } catch (Exception e) {
            log.error("Error retrieving recent orders for customer {}: {}", 
                    customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get customer summary statistics.
     * 
     * @param customerId the customer ID
     * @return customer statistics including total orders, total spent, etc.
     */
    @GetMapping("/{customerId}/summary")
    public ResponseEntity<CustomerSummaryResponse> getCustomerSummary(
            @PathVariable @NotNull @Positive Long customerId) {
        
        log.info("Retrieving summary for customer ID: {}", customerId);
        
        try {
            // Verify customer exists
            if (!customerService.customerExists(customerId)) {
                log.warn("Customer not found with ID: {}", customerId);
                return ResponseEntity.notFound().build();
            }
            
            CustomerSummaryResponse summary = customerService.getCustomerSummary(customerId);
            
            