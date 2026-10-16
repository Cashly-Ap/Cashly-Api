package com.cashly.cashly_api.invoices.reporting.application.generate_invoices_report_pdf.use_case;

import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportFilter;


public interface GenerateInvoicesReportPdfUseCase {
    byte[] execute(InvoiceReportFilter filter, String mode);
}
