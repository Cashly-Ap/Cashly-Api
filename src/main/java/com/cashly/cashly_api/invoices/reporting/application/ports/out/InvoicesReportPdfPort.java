package com.cashly.cashly_api.invoices.reporting.application.ports.out;

import com.cashly.cashly_api.invoices.reporting.application.dto.InvoiceReportRow;

import java.util.List;

public interface InvoicesReportPdfPort {
    byte[] generatePdf(List<InvoiceReportRow> invoices, String mode);
}
