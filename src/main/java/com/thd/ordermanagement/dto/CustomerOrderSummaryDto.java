package com.thd.ordermanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class CustomerOrderSummaryDto {

    private String customerEmail;
    private long totalOrders;
    private BigDecimal totalAmountSpent;
    private Map<String, Long> ordersByStatus;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;

    public CustomerOrderSummaryDto() {
    }

    public CustomerOrderSummaryDto(String customerEmail, long totalOrders, BigDecimal totalAmountSpent,
                                   Map<String, Long> ordersByStatus, LocalDateTime firstOrderDate,
                                   LocalDateTime lastOrderDate) {
        this.customerEmail = customerEmail;
        this.totalOrders = totalOrders;
        this.totalAmountSpent = totalAmountSpent;
        this.ordersByStatus = ordersByStatus;
        this.firstOrderDate = firstOrderDate;
        this.lastOrderDate = lastOrderDate;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalAmountSpent() {
        return totalAmountSpent;
    }

    public void setTotalAmountSpent(BigDecimal totalAmountSpent) {
        this.totalAmountSpent = totalAmountSpent;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public LocalDateTime getFirstOrderDate() {
        return firstOrderDate;
    }

    public void setFirstOrderDate(LocalDateTime firstOrderDate) {
        this.firstOrderDate = firstOrderDate;
    }

    public LocalDateTime getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDateTime lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }
}
