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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing customer order operations.
 * Provides business logic for retrieving customer orders and order summaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerOrderService {

    private final OrderRepository orderRepository;

    /**
     * Retrieves all orders for a specific customer email with pagination support.
     *
     * @param email the customer email address
     * @param fromDate optional start date filter
     * @param toDate optional end date filter
     * @param pageable pagination information
     * @return Page of orders for the customer
     * @throws CustomerNotFoundException if no orders found for the given email
     */
    public Page<Order> getCustomerOrders(@NotBlank @Email String email, 
                                       LocalDate fromDate, 
                                       LocalDate toDate, 
                                       Pageable pageable) {
        log.info("Retrieving orders for customer email: {} with date range: {} to {}", 
                email, fromDate, toDate);
        
        validateEmail(email);
        
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        Page<Order> orders;
        
        if (fromDateTime != null && toDateTime != null) {
            orders = orderRepository.findByCustomerEmailAndOrderDateBetween(
                    email, fromDateTime, toDateTime, pageable);
        } else if (fromDateTime != null) {
            orders = orderRepository.findByCustomerEmailAndOrderDateGreaterThanEqual(
                    email, fromDateTime, pageable);
        } else if (toDateTime != null) {
            orders = orderRepository.findByCustomerEmailAndOrderDateLessThanEqual(
                    email, toDateTime, pageable);
        } else {
            orders = orderRepository.findByCustomerEmail(email, pageable);
        }
        
        if (orders.isEmpty()) {
            log.warn("No orders found for customer email: {}", email);
            throw new CustomerNotFoundException("No orders found for customer email: " + email);
        }
        
        log.info("Found {} orders for customer email: {}", orders.getTotalElements(), email);
        return orders;
    }

    /**
     * Retrieves order history summary for a specific customer email.
     *
     * @param email the customer email address
     * @return CustomerOrderSummaryDto containing order summary information
     * @throws CustomerNotFoundException if no orders found for the given email
     */
    public CustomerOrderSummaryDto getCustomerOrderSummary(@NotBlank @Email String email) {
        log.info("Retrieving order summary for customer email: {}", email);
        
        validateEmail(email);
        
        List<Order> orders = orderRepository.findByCustomerEmailOrderByOrderDateAsc(email);
        
        if (orders.isEmpty()) {
            log.warn("No orders found for customer email: {}", email);
            throw new CustomerNotFoundException("No orders found for customer email: " + email);
        }
        
        CustomerOrderSummaryDto summary = buildOrderSummary(email, orders);
        
        log.info("Generated order summary for customer email: {} - Total orders: {}, Total amount: {}", 
                email, summary.getTotalOrders(), summary.getTotalAmountSpent());
        
        return summary;
    }

    /**
     * Builds order summary from the list of orders.
     *
     * @param email the customer email
     * @param orders list of customer orders
     * @return CustomerOrderSummaryDto with calculated summary data
     */
    private CustomerOrderSummaryDto buildOrderSummary(String email, List<Order> orders) {
        log.debug("Building order summary for {} orders", orders.size());
        
        int totalOrders = orders.size();
        
        BigDecimal totalAmountSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus().name(),
                        Collectors.counting()
                ));
        
        LocalDateTime firstOrderDate = orders.get(0).getOrderDate();
        LocalDateTime lastOrderDate = orders.get(orders.size() - 1).getOrderDate();
        
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
     * Validates email format and ensures it's not empty.
     *
     * @param email the email to validate
     * @throws IllegalArgumentException if email is invalid
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.error("Email address is required but was empty or null");
            throw new IllegalArgumentException("Email address is required");
        }
        
        // Basic email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            log.error("Invalid email format provided: {}", email);
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        log.debug("Email validation passed for: {}", email);
    }

    /**
     * Checks if a customer has any orders.
     *
     * @param email the customer email address
     * @return true if customer has orders, false otherwise
     */
    public boolean hasOrders(@NotBlank @Email String email) {
        log.debug("Checking if customer has orders: {}", email);
        
        validateEmail(email);
        
        boolean exists = orderRepository.existsByCustomerEmail(email);
        
        log.debug("Customer {} has orders: {}", email, exists);
        return exists;
    }

    /**
     * Gets the total number of orders for a customer.
     *
     * @param email the customer email address
     * @return total count of orders
     */
    public long getCustomerOrderCount(@NotBlank @Email String email) {
        log.debug("Getting order count for customer: {}", email);
        
        validateEmail(email);
        
        long count = orderRepository.countByCustomerEmail(email);
        
        log.debug("Customer {} has {} orders", email, count);
        return count;
    }
}
```