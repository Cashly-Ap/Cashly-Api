package com.cashly.cashly_api.invoices.infrastructure.services;

import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class InvoiceStatusServiceImpl implements InvoiceStatusService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final CreditNoteRepository creditNoteRepository;

    public InvoiceStatusServiceImpl(InvoiceRepository invoiceRepository,
                                     PaymentAllocationRepository paymentAllocationRepository,
                                     DebitNoteRepository debitNoteRepository,
                                     CreditNoteRepository creditNoteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.creditNoteRepository = creditNoteRepository;
    }

    @Override
    @Transactional
    public void recalculate(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal totalPaid = paymentAllocationRepository.findByInvoiceId(invoiceId).stream()
                .map(a -> a.getAllocatedAmount() != null ? a.getAllocatedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = debitNoteRepository.findByInvoiceId(invoiceId).stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditNotes = creditNoteRepository.findByInvoiceId(invoiceId).stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.recalculatePaymentStatus(totalPaid, totalDebitNotes, totalCreditNotes);
        invoiceRepository.save(invoice);
    }
}
