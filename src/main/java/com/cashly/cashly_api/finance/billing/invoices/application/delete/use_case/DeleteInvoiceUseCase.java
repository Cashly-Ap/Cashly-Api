package com.cashly.cashly_api.finance.billing.invoices.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceOrderGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceOrderGateway orders;
    private final InventoryGateway inventoryGateway;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteInvoiceUseCase(InvoiceRepository invoiceRepository,
            InvoiceOrderGateway orders, InventoryGateway inventoryGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceRepository = invoiceRepository;
        this.orders = orders;
        this.inventoryGateway = inventoryGateway;
    }

    @Transactional
    public void execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        invoice.delete();
        revertLinkedOrder(invoice);
        revertStockAllocations(invoice);

        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_INVOICES",
                "DELETE", "INVOICE", invoice.getId().value(),
                invoice.getInvoiceNumber().value(), null));
        invoiceRepository.deleteById(InvoiceId.of(id));
    }

    private void revertLinkedOrder(Invoice invoice) {
        if (invoice.getOrderId() != null) {
            orders.revertInvoiceLink(invoice.getOrderId());
        }
    }

    private void revertStockAllocations(Invoice invoice) {
        inventoryGateway.releaseInvoiceReservations(
                InventoryGateway.InvoiceInventoryCommand.from(invoice));
    }
}
