package com.cashly.cashly_api.invoices.application.contracts;
import java.util.UUID;
public interface InvoiceNotificationGateway {
    void notifyCustomerInvoiceCreated(UUID customerId, UUID invoiceId, String invoiceNumber);
    void notifyCustomerInvoiceConfirmed(UUID customerId, UUID invoiceId, String invoiceNumber);
    void notifyCustomerInvoiceCancelled(UUID customerId, UUID invoiceId, String invoiceNumber, String reason);
}
