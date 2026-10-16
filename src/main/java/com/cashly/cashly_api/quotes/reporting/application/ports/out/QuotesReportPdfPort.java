package com.cashly.cashly_api.quotes.reporting.application.ports.out;

import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportRow;

import java.util.List;


public interface QuotesReportPdfPort {
    byte[] generatePdf(List<QuoteReportRow> quotes, String mode);
}
