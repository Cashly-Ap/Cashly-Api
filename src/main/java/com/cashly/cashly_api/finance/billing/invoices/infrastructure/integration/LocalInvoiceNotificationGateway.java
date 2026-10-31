package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNotificationGateway;
import com.cashly.cashly_api.platform.notifications.application.services.NotificationService;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class LocalInvoiceNotificationGateway
        implements
            InvoiceNotificationGateway {
    private final NotificationService notifications;
    public LocalInvoiceNotificationGateway(NotificationService notifications) {
        this.notifications = notifications;
    }
    public void notifyCustomerInvoiceCreated(UUID customerId, UUID invoiceId,
            String invoiceNumber) {
        notifications.notifyCustomerInvoiceCreated(customerId, invoiceId,
                invoiceNumber);
    }
    public void notifyCustomerInvoiceConfirmed(UUID customerId, UUID invoiceId,
            String invoiceNumber) {
        notifications.notifyCustomerInvoiceConfirmed(customerId, invoiceId,
                invoiceNumber);
    }
    public void notifyCustomerInvoiceCancelled(UUID customerId, UUID invoiceId,
            String invoiceNumber, String reason) {
        notifications.notifyCustomerInvoiceCancelled(customerId, invoiceId,
                invoiceNumber, reason);
    }
}
