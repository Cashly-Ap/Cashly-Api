package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportData;
import com.cashly.cashly_api.reports.invoices.application.contracts.InvoiceReportQueryGateway;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicePdfPort;

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
