package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportFilter;
import com.cashly.cashly_api.reports.invoices.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.reports.invoices.application.ports.out.InvoicesReportPdfPort;


public class GenerateInvoicesReportPdfUseCaseImpl implements GenerateInvoicesReportPdfUseCase {

    private final InvoiceReportQueryGateway query;
    private final InvoicesReportPdfPort invoicesReportPdfPort;

    public GenerateInvoicesReportPdfUseCaseImpl(InvoiceReportQueryGateway query, InvoicesReportPdfPort invoicesReportPdfPort) {
        this.query = query;
        this.invoicesReportPdfPort = invoicesReportPdfPort;
    }

    @Override
    public byte[] execute(InvoiceReportFilter filter, String mode) {
        return invoicesReportPdfPort.generatePdf(query.findAll(filter), mode != null ? mode : "simple");
    }
}
