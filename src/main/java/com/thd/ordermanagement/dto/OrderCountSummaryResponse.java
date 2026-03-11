package com.thd.ordermanagement.dto;

import java.util.Map;

public class OrderCountSummaryResponse {

    private long totalOrders;
    private Map<String, Long> statusCounts;

    public OrderCountSummaryResponse() {
    }

    public OrderCountSummaryResponse(long totalOrders, Map<String, Long> statusCounts) {
        this.totalOrders = totalOrders;
        this.statusCounts = statusCounts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }
}
