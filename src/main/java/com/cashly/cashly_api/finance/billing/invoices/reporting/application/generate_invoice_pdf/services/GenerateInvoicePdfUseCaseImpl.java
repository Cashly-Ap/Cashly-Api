package com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoice_pdf.services;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoice_pdf.use_case.GenerateInvoicePdfUseCase;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportData;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicePdfPort;

import java.util.UUID;

public class GenerateInvoicePdfUseCaseImpl implements GenerateInvoicePdfUseCase {

    private final InvoiceReportQueryGateway query;
    private final InvoicePdfPort invoicePdfPort;

    public GenerateInvoicePdfUseCaseImpl(InvoiceReportQueryGateway query,
                                          InvoicePdfPort invoicePdfPort) {
        this.query = query;
        this.invoicePdfPort = invoicePdfPort;
    }

    @Override
    public byte[] execute(UUID invoiceId) {
        InvoiceReportData invoice = query.find(invoiceId);
        return invoicePdfPort.generatePdf(invoice);
    }
}
