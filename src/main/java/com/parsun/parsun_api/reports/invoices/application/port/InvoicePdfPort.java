package com.parsun.parsun_api.reports.invoices.application.port;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;

public interface InvoicePdfPort {
    byte[] generatePdf(InvoiceDetailResponse invoice);
}
