package com.cashly.cashly_api.quotes.reporting.application.ports.out;

import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportData;


public interface QuotePdfPort {
    byte[] generatePdf(QuoteReportData quote);
}
