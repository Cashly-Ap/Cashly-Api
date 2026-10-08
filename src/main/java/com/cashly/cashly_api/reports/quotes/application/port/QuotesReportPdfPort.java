package com.cashly.cashly_api.reports.quotes.application.port;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportRow;

import java.util.List;


public interface QuotesReportPdfPort {
    byte[] generatePdf(List<QuoteReportRow> quotes, String mode);
}
