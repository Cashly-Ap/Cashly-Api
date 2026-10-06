package com.cashly.cashly_api.reports.invoices.application.port;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceListResponse;

import java.util.List;

public interface InvoicesReportPdfPort {
    byte[] generatePdf(List<InvoiceListResponse> invoices, String mode);
}
