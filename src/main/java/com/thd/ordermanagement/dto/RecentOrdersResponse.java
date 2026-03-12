package com.thd.ordermanagement.dto;

import java.util.List;

public class RecentOrdersResponse {

    private List<OrderResponse> orders;
    private int count;

    public RecentOrdersResponse() {
    }

    public RecentOrdersResponse(List<OrderResponse> orders, int count) {
        this.orders = orders;
        this.count = count;
    }

    public List<OrderResponse> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderResponse> orders) {
        this.orders = orders;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}