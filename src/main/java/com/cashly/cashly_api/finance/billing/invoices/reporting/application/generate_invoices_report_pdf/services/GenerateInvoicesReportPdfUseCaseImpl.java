package com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoices_report_pdf.services;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoices_report_pdf.use_case.GenerateInvoicesReportPdfUseCase;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportFilter;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicesReportPdfPort;

public class GenerateInvoicesReportPdfUseCaseImpl
        implements
            GenerateInvoicesReportPdfUseCase {

    private final InvoiceReportQueryGateway query;
    private final InvoicesReportPdfPort invoicesReportPdfPort;

    public GenerateInvoicesReportPdfUseCaseImpl(InvoiceReportQueryGateway query,
            InvoicesReportPdfPort invoicesReportPdfPort) {
        this.query = query;
        this.invoicesReportPdfPort = invoicesReportPdfPort;
    }

    @Override
    public byte[] execute(InvoiceReportFilter filter, String mode) {
        return invoicesReportPdfPort.generatePdf(query.findAll(filter),
                mode != null ? mode : "simple");
    }
}
