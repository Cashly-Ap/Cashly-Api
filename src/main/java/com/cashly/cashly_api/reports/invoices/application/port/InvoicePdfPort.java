package com.cashly.cashly_api.reports.invoices.application.port;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportData;

public interface InvoicePdfPort {
    byte[] generatePdf(InvoiceReportData invoice);
}
