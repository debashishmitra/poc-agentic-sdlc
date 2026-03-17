package com.thd.ordermanagement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.thd.ordermanagement.dto.CustomerOrderSummaryDto;
import com.thd.ordermanagement.exception.CustomerNotFoundException;
import com.thd.ordermanagement.model.Order;
import com.thd.ordermanagement.repository.OrderRepository;

@Service
@Transactional(readOnly = true)
public class CustomerOrderService {

    private static final Logger log = LoggerFactory.getLogger(CustomerOrderService.class);

    private final OrderRepository orderRepository;

    public CustomerOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<Order> getCustomerOrders(String email, LocalDateTime fromDate, LocalDateTime toDate,
                                         Pageable pageable) {
        log.info("Retrieving orders for customer email: {} with date range from {} to {}", email, fromDate, toDate);

        validateEmail(email);

        Page<Order> orders;

        if (fromDate != null && toDate != null) {
            validateDateRange(fromDate, toDate);
            orders = orderRepository.findByCustomerEmailAndCreatedAtBetween(email, fromDate, toDate, pageable);
        } else if (fromDate != null) {
            orders = orderRepository.findByCustomerEmailAndCreatedAtGreaterThanEqual(email, fromDate, pageable);
        } else if (toDate != null) {
            orders = orderRepository.findByCustomerEmailAndCreatedAtLessThanEqual(email, toDate, pageable);
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

    public CustomerOrderSummaryDto getCustomerOrderSummary(String email) {
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

    public boolean hasCustomerOrders(String email) {
        log.debug("Checking if customer {} has orders", email);
        validateEmail(email);
        boolean hasOrders = orderRepository.existsByCustomerEmail(email);
        log.debug("Customer {} has orders: {}", email, hasOrders);
        return hasOrders;
    }

    private CustomerOrderSummaryDto buildOrderSummary(String email, List<Order> orders) {
        BigDecimal totalAmountSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderStatus().name(),
                        Collectors.counting()
                ));

        LocalDateTime firstOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new CustomerOrderSummaryDto(email, orders.size(), totalAmountSpent,
                ordersByStatus, firstOrderDate, lastOrderDate);
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Customer email cannot be null or empty");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private void validateDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
    }
}
