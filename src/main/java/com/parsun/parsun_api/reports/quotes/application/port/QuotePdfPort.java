package com.parsun.parsun_api.reports.quotes.application.port;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;

/**
 * Port for generating PDF for a single quote.
 */
public interface QuotePdfPort {
    byte[] generatePdf(QuoteDetailResponse quote);
}
