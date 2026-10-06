package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceListResponse;

import java.util.List;

public interface GenerateInvoicesReportPdfUseCase {
    byte[] execute(List<InvoiceListResponse> invoices, String mode);
}
