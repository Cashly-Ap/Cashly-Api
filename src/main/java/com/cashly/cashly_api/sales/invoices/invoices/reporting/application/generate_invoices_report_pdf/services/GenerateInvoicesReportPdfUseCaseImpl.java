package com.cashly.cashly_api.sales.invoices.invoices.reporting.application.generate_invoices_report_pdf.services;

import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.generate_invoices_report_pdf.use_case.GenerateInvoicesReportPdfUseCase;

import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.dto.model.InvoiceReportFilter;
import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateInvoicesReportPdfUseCaseImpl
        implements
            GenerateInvoicesReportPdfUseCase {

    private static final Logger log = LoggerFactory
            .getLogger(GenerateInvoicesReportPdfUseCaseImpl.class);

    private final InvoiceReportQueryGateway query;
    private final InvoicesReportPdfPort invoicesReportPdfPort;

    public GenerateInvoicesReportPdfUseCaseImpl(InvoiceReportQueryGateway query,
            InvoicesReportPdfPort invoicesReportPdfPort) {
        this.query = query;
        this.invoicesReportPdfPort = invoicesReportPdfPort;
    }

    @Override
    public byte[] execute(InvoiceReportFilter filter, String mode) {
        long started = System.nanoTime();
        var rows = query.findAll(filter);
        long afterQuery = System.nanoTime();
        byte[] pdf = invoicesReportPdfPort.generatePdf(rows,
                mode != null ? mode : "simple");
        long afterPdf = System.nanoTime();
        log.info("invoices-report rows={} query={} ms pdf={} ms size={}",
                rows.size(), (afterQuery - started) / 1_000_000,
                (afterPdf - afterQuery) / 1_000_000, pdf.length);
        return pdf;
    }
}
