package com.cashly.cashly_api.sales.quotes.reporting.application.generate_quotes_report_pdf.use_case;

import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportFilter;



public interface GenerateQuotesReportPdfUseCase {
    byte[] execute(QuoteReportFilter filter, String mode);
}
