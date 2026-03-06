package com.thd.ordermanagement.dto;

import com.thd.ordermanagement.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {

    @NotNull(message = "Status cannot be null")
    private OrderStatus status;

    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequest{" +
                "status=" + status +
                '}';
    }
}
