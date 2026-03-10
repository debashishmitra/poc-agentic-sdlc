```java
package com.example.orderservice.service;

import com.example.orderservice.dto.CustomerOrderSummaryDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.exception.CustomerNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing customer order operations.
 * Provides business logic for retrieving customer orders and order summaries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerOrderService {

    private final OrderRepository orderRepository;

    /**
     * Retrieves all orders for a given customer email with pagination and optional date filtering.
     *
     * @param email the customer's email address
     * @param fromDate optional start date for filtering orders
     * @param toDate optional end date for filtering orders
     * @param pageable pagination information
     * @return paginated list of orders for the customer
     * @throws CustomerNotFoundException if no orders found for the given email
     */
    public Page<Order> getCustomerOrders(
            @NotBlank @Email String email,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        
        log.info("Retrieving orders for customer email: {} with date range from {} to {}", 
                email, fromDate, toDate);
        
        validateEmail(email);
        
        Page<Order> orders;
        
        if (fromDate != null && toDate != null) {
            validateDateRange(fromDate, toDate);
            orders = orderRepository.findByCustomerEmailAndCreatedDateBetween(
                    email, fromDate, toDate, pageable);
        } else if (fromDate != null) {
            orders = orderRepository.findByCustomerEmailAndCreatedDateGreaterThanEqual(
                    email, fromDate, pageable);
        } else if (toDate != null) {
            orders = orderRepository.findByCustomerEmailAndCreatedDateLessThanEqual(
                    email, toDate, pageable);
        } else {
            orders = orderRepository.findByCustomerEmail(email, pageable);
        }
        
        if (orders.isEmpty()) {
            log.warn("No orders found for customer email: {}", email);
            throw new CustomerNotFoundException("No orders found for customer: " + email);
        }
        
        log.info("Found {} orders for customer email: {}", orders.getTotalElements(), email);
        return orders;
    }

    /**
     * Retrieves order history summary for a given customer email.
     *
     * @param email the customer's email address
     * @return customer order summary containing aggregated order statistics
     * @throws CustomerNotFoundException if no orders found for the given email
     */
    public CustomerOrderSummaryDto getCustomerOrderSummary(@NotBlank @Email String email) {
        log.info("Retrieving order summary for customer email: {}", email);
        
        validateEmail(email);
        
        List<Order> orders = orderRepository.findByCustomerEmail(email);
        
        if (orders.isEmpty()) {
            log.warn("No orders found for customer email: {}", email);
            throw new CustomerNotFoundException("No orders found for customer: " + email);
        }
        
        CustomerOrderSummaryDto summary = buildOrderSummary(email, orders);
        
        log.info("Generated order summary for customer email: {} - Total orders: {}, Total amount: {}", 
                email, summary.getTotalOrders(), summary.getTotalAmountSpent());
        
        return summary;
    }

    /**
     * Builds order summary from the list of orders.
     *
     * @param email customer email
     * @param orders list of customer orders
     * @return customer order summary DTO
     */
    private CustomerOrderSummaryDto buildOrderSummary(String email, List<Order> orders) {
        log.debug("Building order summary for {} orders", orders.size());
        
        long totalOrders = orders.size();
        
        BigDecimal totalAmountSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus().name(),
                        Collectors.counting()
                ));
        
        LocalDateTime firstOrderDate = orders.stream()
                .map(Order::getCreatedDate)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastOrderDate = orders.stream()
                .map(Order::getCreatedDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return CustomerOrderSummaryDto.builder()
                .customerEmail(email)
                .totalOrders(totalOrders)
                .totalAmountSpent(totalAmountSpent)
                .ordersByStatus(ordersByStatus)
                .firstOrderDate(firstOrderDate)
                .lastOrderDate(lastOrderDate)
                .build();
    }

    /**
     * Validates email format and non-null value.
     *
     * @param email the email to validate
     * @throws IllegalArgumentException if email is invalid
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.error("Email validation failed: email is null or empty");
            throw new IllegalArgumentException("Customer email cannot be null or empty");
        }
        
        // Additional email format validation can be added here if needed
        // The @Email annotation on method parameters provides basic validation
        if (!email.contains("@")) {
            log.error("Email validation failed: invalid email format - {}", email);
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Validates date range parameters.
     *
     * @param fromDate start date
     * @param toDate end date
     * @throws IllegalArgumentException if date range is invalid
     */
    private void validateDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            log.error("Date range validation failed: fromDate {} is after toDate {}", fromDate, toDate);
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        
        if (fromDate.isAfter(LocalDateTime.now())) {
            log.error("Date range validation failed: fromDate {} is in the future", fromDate);
            throw new IllegalArgumentException("From date cannot be in the future");
        }
    }

    /**
     * Checks if a customer has any orders.
     *
     * @param email customer email
     * @return true if customer has orders, false otherwise
     */
    public boolean hasCustomerOrders(@NotBlank @Email String email) {
        log.debug("Checking if customer {} has orders", email);
        
        validateEmail(email);
        
        boolean hasOrders = orderRepository.existsByCustomerEmail(email);
        
        log.debug("Customer {} has orders: {}", email, hasOrders);
        return hasOrders;
    }
}
```