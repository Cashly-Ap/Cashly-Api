package com.cashly.cashly_api.reports.invoices.application.ports.out;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportRow;

import java.util.List;

public interface InvoicesReportPdfPort {
    byte[] generatePdf(List<InvoiceReportRow> invoices, String mode);
}
