package com.cashly.cashly_api.reports.quotes.application.usecases;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportFilter;



public interface GenerateQuotesReportPdfUseCase {
    byte[] execute(QuoteReportFilter filter, String mode);
}
