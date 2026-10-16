package com.cashly.cashly_api.quotes.reporting.application.generate_quotes_report_pdf;

import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportFilter;



public interface GenerateQuotesReportPdfUseCase {
    byte[] execute(QuoteReportFilter filter, String mode);
}
