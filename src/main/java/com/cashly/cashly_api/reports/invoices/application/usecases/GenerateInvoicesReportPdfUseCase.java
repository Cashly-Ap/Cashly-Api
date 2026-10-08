package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportFilter;


public interface GenerateInvoicesReportPdfUseCase {
    byte[] execute(InvoiceReportFilter filter, String mode);
}
