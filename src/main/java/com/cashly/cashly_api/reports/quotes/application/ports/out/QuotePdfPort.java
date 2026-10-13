package com.cashly.cashly_api.reports.quotes.application.ports.out;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportData;


public interface QuotePdfPort {
    byte[] generatePdf(QuoteReportData quote);
}
