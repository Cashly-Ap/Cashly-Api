package com.cashly.cashly_api.invoices.infrastructure.services;

import com.cashly.cashly_api.invoices.application.ports.out.InvoiceFinancialPositionGateway;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class InvoiceStatusServiceImpl implements InvoiceStatusService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceFinancialPositionGateway financialPosition;

    public InvoiceStatusServiceImpl(InvoiceRepository invoiceRepository,
                                     InvoiceFinancialPositionGateway financialPosition) {
        this.invoiceRepository = invoiceRepository;
        this.financialPosition = financialPosition;
    }

    @Override
    @Transactional
    public void recalculate(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        InvoiceFinancialPositionGateway.Position position = financialPosition.findByInvoiceId(invoiceId);
        invoice.recalculatePaymentStatus(position.paid(), position.debitNotes(), position.creditNotes());
        invoiceRepository.save(invoice);
    }
}
