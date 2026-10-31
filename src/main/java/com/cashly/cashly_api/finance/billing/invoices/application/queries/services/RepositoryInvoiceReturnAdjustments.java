package com.cashly.cashly_api.finance.billing.invoices.application.queries.services;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnAdjustments;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnCommand;

import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceReturnAdjustment;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public class RepositoryInvoiceReturnAdjustments
        implements
            InvoiceReturnAdjustments {
    private final InvoiceRepository invoiceRepository;

    public RepositoryInvoiceReturnAdjustments(
            InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public void apply(InvoiceReturnCommand command) {
        Invoice invoice = find(command);
        invoice.applyReturn(adjustment(command));
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void reverse(InvoiceReturnCommand command, BigDecimal totalPaid,
            BigDecimal totalDebitNotes, BigDecimal totalCreditNotes) {
        Invoice invoice = find(command);
        invoice.revertReturn(adjustment(command));
        invoice.recalculatePaymentStatus(totalPaid, totalDebitNotes,
                totalCreditNotes);
        invoiceRepository.save(invoice);
    }

    private Invoice find(InvoiceReturnCommand command) {
        return invoiceRepository.findById(InvoiceId.of(command.invoiceId()))
                .orElseThrow(() -> new InvoiceNotFoundException(
                        command.invoiceId()));
    }

    private InvoiceReturnAdjustment adjustment(InvoiceReturnCommand command) {
        return new InvoiceReturnAdjustment(command.invoiceId(), command.total(),
                command.lines().stream()
                        .map(line -> new InvoiceReturnAdjustment.Line(
                                line.invoiceLineId(), line.quantity()))
                        .toList(),
                command.closeInvoice());
    }
}
