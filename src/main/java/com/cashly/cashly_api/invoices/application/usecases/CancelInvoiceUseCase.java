package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.invoices.application.contracts.InventoryGateway;
import com.cashly.cashly_api.invoices.application.dto.request.CancelInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceNotificationGateway;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CancelInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InventoryGateway inventoryGateway;
    private final InvoiceNotificationGateway notifications;

    public CancelInvoiceUseCase(InvoiceRepository invoiceRepository,
                                 InventoryGateway inventoryGateway,
                                 InvoiceNotificationGateway notifications) {
        this.invoiceRepository = invoiceRepository;
        this.inventoryGateway = inventoryGateway;
        this.notifications = notifications;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, CancelInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        InvoiceStatus previousStatus = invoice.getStatus();
        invoice.cancel(request.reason());
        revertStockAllocations(invoice, previousStatus);

        Invoice saved = invoiceRepository.save(invoice);
        notifications.notifyCustomerInvoiceCancelled(
                saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value(), request.reason());
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Invoice invoice, InvoiceStatus previousStatus) {
        inventoryGateway.cancelInvoice(InventoryGateway.InvoiceInventoryCommand.from(invoice),
                previousStatus != InvoiceStatus.DRAFT);
    }
}
