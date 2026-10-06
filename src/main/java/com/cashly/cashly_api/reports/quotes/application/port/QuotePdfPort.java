package com.cashly.cashly_api.reports.quotes.application.port;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;


public interface QuotePdfPort {
    byte[] generatePdf(QuoteDetailResponse quote);
}
