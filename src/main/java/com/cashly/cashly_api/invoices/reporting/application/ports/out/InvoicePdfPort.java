package com.cashly.cashly_api.invoices.reporting.application.ports.out;

import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportData;

public interface InvoicePdfPort {
    byte[] generatePdf(InvoiceReportData invoice);
}
