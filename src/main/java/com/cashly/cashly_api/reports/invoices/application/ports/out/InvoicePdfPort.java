package com.cashly.cashly_api.reports.invoices.application.ports.out;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportData;

public interface InvoicePdfPort {
    byte[] generatePdf(InvoiceReportData invoice);
}
