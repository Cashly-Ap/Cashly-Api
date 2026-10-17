package com.cashly.cashly_api.sales.orders.domain.exceptions;

public class OrderNotPendingException extends RuntimeException {

    public OrderNotPendingException(String action) {
        super("Cannot " + action + " an order that is not in PENDING status");
    }
}
