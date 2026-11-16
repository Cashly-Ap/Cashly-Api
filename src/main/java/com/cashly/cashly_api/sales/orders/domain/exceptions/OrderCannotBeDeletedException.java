package com.cashly.cashly_api.sales.orders.domain.exceptions;

import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;

import java.util.UUID;

public class OrderCannotBeDeletedException extends RuntimeException {

    public OrderCannotBeDeletedException(UUID id, OrderStatus status) {
        super("Order " + id + " cannot be deleted because it is in status "
                + status.name());
    }
}
