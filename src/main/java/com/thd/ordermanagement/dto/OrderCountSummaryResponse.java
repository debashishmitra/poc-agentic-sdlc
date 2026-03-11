package com.thd.ordermanagement.dto;

import com.thd.ordermanagement.model.OrderStatus;

import java.util.Map;

public class OrderCountSummaryResponse {

    private Map<OrderStatus, Long> statusCounts;
    private long totalOrders;

    public OrderCountSummaryResponse() {
    }

    public OrderCountSummaryResponse(Map<OrderStatus, Long> statusCounts, long totalOrders) {
        this.statusCounts = statusCounts;
        this.totalOrders = totalOrders;
    }

    public Map<OrderStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<OrderStatus, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }
}
