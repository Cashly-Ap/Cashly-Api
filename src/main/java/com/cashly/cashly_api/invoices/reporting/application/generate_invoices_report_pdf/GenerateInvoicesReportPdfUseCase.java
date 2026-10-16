package com.cashly.cashly_api.invoices.reporting.application.generate_invoices_report_pdf;

import com.cashly.cashly_api.invoices.reporting.application.dto.InvoiceReportFilter;


public interface GenerateInvoicesReportPdfUseCase {
    byte[] execute(InvoiceReportFilter filter, String mode);
}
