package com.cashly.cashly_api.finance.billing.invoices.application.cancel.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CashRegisterGateway;
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
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class CancelInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InventoryGateway inventoryGateway;
    private final CashRegisterGateway cashRegister;

    private final ActivityNotificationRecorder activityNotifications;

    public CancelInvoiceUseCase(InvoiceRepository invoiceRepository,
            InventoryGateway inventoryGateway,
            ActivityNotificationRecorder activityNotifications,
            CashRegisterGateway cashRegister) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceRepository = invoiceRepository;
        this.inventoryGateway = inventoryGateway;
        this.cashRegister = cashRegister;
    }

    @Transactional
    @Audited(module = "finance", entityType = "INVOICE", action = "CANCEL")
    public InvoiceDetailResponse execute(UUID id,
            CancelInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        InvoiceStatus previousStatus = invoice.getStatus();
        invoice.cancel(request.reason());
        revertStockAllocations(invoice, previousStatus);

        Invoice saved = invoiceRepository.save(invoice);
        if (cashRegister != null) {
            cashRegister.cancelInvoice(saved.getId().value());
        }
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
