package com.cashly.cashly_api.quotes.reporting.application.ports.out;

import com.cashly.cashly_api.quotes.reporting.application.dto.model.QuoteReportData;


public interface QuotePdfPort {
    byte[] generatePdf(QuoteReportData quote);
}
