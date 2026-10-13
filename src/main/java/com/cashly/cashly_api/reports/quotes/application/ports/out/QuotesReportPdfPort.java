package com.cashly.cashly_api.reports.quotes.application.ports.out;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportRow;

import java.util.List;


public interface QuotesReportPdfPort {
    byte[] generatePdf(List<QuoteReportRow> quotes, String mode);
}
