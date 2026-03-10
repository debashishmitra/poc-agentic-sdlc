```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CustomerSearchRequest;
import com.example.ecommerce.dto.CustomerResponse;
import com.example.ecommerce.dto.OrderHistoryResponse;
import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.exception.CustomerNotFoundException;
import com.example.ecommerce.exception.InvalidRequestException;
import com.example.ecommerce.service.CustomerService;
import com.example.ecommerce.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * REST Controller for customer search and order history operations
 * Handles customer-related API endpoints including search functionality and order history retrieval
 * 
 * @author Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/customers")
@Validated
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    
    private final CustomerService customerService;
    private final OrderService orderService;

    /**
     * Constructor injection for required services
     * 
     * @param customerService Service for customer operations
     * @param orderService Service for order operations
     */
    @Autowired
    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    /**
     * Search customers based on various criteria
     * Supports pagination and sorting
     * 
     * @param searchRequest Customer search criteria
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @return Paginated list of customers matching search criteria
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> searchCustomers(
            @Valid @RequestBody CustomerSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.info("Searching customers with criteria: {}, page: {}, size: {}", searchRequest, page, size);
        
        try {
            // Validate sort direction
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Perform search
            Page<CustomerResponse> customers = customerService.searchCustomers(searchRequest, pageable);
            
            logger.info("Found {} customers matching search criteria", customers.getTotalElements());
            
            ApiResponse<Page<CustomerResponse>> response = new ApiResponse<>(
                true,
                "Customers retrieved successfully",
                customers
            );
            
            return ResponseEntity.ok(response);
            
        } catch (InvalidRequestException e) {
            logger.warn("Invalid search request: {}", e.getMessage());
            
            ApiResponse<Page<CustomerResponse>> errorResponse = new ApiResponse<>(
                false,
                e.getMessage(),
                null
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error searching customers: {}", e.getMessage(), e);
            
            ApiResponse<Page<CustomerResponse>> errorResponse = new ApiResponse<>(
                false,
                "Internal server error occurred while searching customers",
                null
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get customer by ID
     * 
     * @param customerId Customer ID
     * @return Customer details
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable @NotNull Long customerId) {
        
        logger.info("Retrieving customer with ID: {}", customerId);
        
        try {
            CustomerResponse customer = customerService.getCustomerById(customerId);
            
            logger.info("Successfully retrieved customer: {}", customer.getEmail());
            
            ApiResponse<CustomerResponse> response = new ApiResponse<>(
                true,
                "Customer retrieved successfully",
                customer
            );
            
            return ResponseEntity.ok(response);
            
        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found with ID: {}", customerId);
            
            ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(
                false,
                e.getMessage(),
                null
            );
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error retrieving customer with ID {}: {}", customerId, e.getMessage(), e);
            
            ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(
                false,
                "Internal server error occurred while retrieving customer",
                null
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get order history for a specific customer
     * Supports pagination and filtering by date range and order status
     * 
     * @param customerId Customer ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @param status Filter by order status
     * @param startDate Filter by start date (yyyy-MM-dd format)
     * @param endDate Filter by end date (yyyy-MM-dd format)
     * @return Paginated order history for the customer
     */
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<ApiResponse<Page<OrderHistoryResponse>>> getCustomerOrderHistory(
            @PathVariable @NotNull Long customerId,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        logger.info("Retrieving order history for customer ID: {}, page: {}, size: {}", 
                   customerId, page, size);
        
        try {
            // Validate customer exists
            customerService.validateCustomerExists(customerId);
            
            // Validate sort direction
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Get order history
            Page<OrderHistoryResponse> orderHistory = orderService.getCustomerOrderHistory(
                customerId, pageable, status, startDate, endDate);
            
            logger.info("Found {} orders for customer ID: {}", 
                       orderHistory.getTotalElements(), customerId);
            
            ApiResponse<Page<OrderHistoryResponse>> response = new ApiResponse<>(
                true,
                "Order history retrieved successfully",
                orderHistory
            );
            
            return ResponseEntity.ok(response);
            
        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found with ID: {}", customerId);
            
            ApiResponse<Page<OrderHistoryResponse>> errorResponse = new ApiResponse<>(
                false,
                e.getMessage(),
                null
            );
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (InvalidRequestException e) {
            logger.warn("Invalid request for order history: {}", e.getMessage());
            
            ApiResponse<Page<OrderHistoryResponse>> errorResponse = new ApiResponse<>(
                false,
                e.getMessage(),
                null
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error retrieving order history for customer ID {}: {}", 
                        customerId, e.getMessage(), e);
            
            ApiResponse<Page<OrderHistoryResponse>> errorResponse = new ApiResponse<>(
                false,
                "Internal server error occurred while retrieving order history",
                null
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get customer summary statistics
     * Includes total orders, total spent, average order value, etc.
     * 
     * @param customerId Customer ID
     * @return Customer summary statistics
     */
    @GetMapping("/{customerId}/summary")
    public ResponseEntity<ApiResponse<CustomerSummaryResponse>> getCustomerSummary(
            @PathVariable @NotNull Long customerId) {
        
        logger.