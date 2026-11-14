package com.cashly.cashly_api.sales.invoices.invoices.reporting.application.ports.out;

import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.dto.model.InvoiceReportRow;

import java.util.List;

public interface InvoicesReportPdfPort {
    byte[] generatePdf(List<InvoiceReportRow> invoices, String mode);
}
