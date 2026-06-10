package com.parsun.parsun_api.reports.quotes.application.port;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;


public interface QuotePdfPort {
    byte[] generatePdf(QuoteDetailResponse quote);
}
