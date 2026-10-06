package com.cashly.cashly_api.reports.invoices.application.port;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;

public interface InvoicePdfPort {
    byte[] generatePdf(InvoiceDetailResponse invoice);
}
