package com.cashly.cashly_api.sales.quotes.reporting.application.ports.out;

import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportData;


public interface QuotePdfPort {
    byte[] generatePdf(QuoteReportData quote);
}
