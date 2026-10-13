package com.cashly.cashly_api.orders.application.ports.out;

import java.util.UUID;

public interface OrderNotificationGateway {
    void notifyCustomerOrderApproved(UUID customerId, UUID orderId);
    void notifyCustomerOrderRejected(UUID customerId, UUID orderId, String reason);
    void notifyCustomerOrderConvertedToInvoice(UUID customerId, UUID orderId, String invoiceNumber);
}
