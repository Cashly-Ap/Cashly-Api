package com.cashly.cashly_api.finance.billing.invoices.application.cancel.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.cancel.request.CancelInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CancelInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InventoryGateway inventoryGateway;

    private final ActivityNotificationRecorder activityNotifications;

    public CancelInvoiceUseCase(InvoiceRepository invoiceRepository,
            InventoryGateway inventoryGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceRepository = invoiceRepository;
        this.inventoryGateway = inventoryGateway;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id,
            CancelInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        InvoiceStatus previousStatus = invoice.getStatus();
        invoice.cancel(request.reason());
        revertStockAllocations(invoice, previousStatus);

        Invoice saved = invoiceRepository.save(invoice);
        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_INVOICES",
                "CANCEL", "INVOICE", saved.getId().value(),
                saved.getInvoiceNumber().value(), null));
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Invoice invoice,
            InvoiceStatus previousStatus) {
        inventoryGateway.cancelInvoice(
                InventoryGateway.InvoiceInventoryCommand.from(invoice),
                previousStatus != InvoiceStatus.DRAFT);
    }
}
