package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicesReportPdfPort;

import java.util.List;

public class GenerateInvoicesReportPdfUseCaseImpl implements GenerateInvoicesReportPdfUseCase {

    private final InvoicesReportPdfPort invoicesReportPdfPort;

    public GenerateInvoicesReportPdfUseCaseImpl(InvoicesReportPdfPort invoicesReportPdfPort) {
        this.invoicesReportPdfPort = invoicesReportPdfPort;
    }

    @Override
    public byte[] execute(List<InvoiceListResponse> invoices, String mode) {
        return invoicesReportPdfPort.generatePdf(invoices, mode != null ? mode : "simple");
    }
}
