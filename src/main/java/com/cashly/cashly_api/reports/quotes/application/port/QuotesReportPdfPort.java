package com.cashly.cashly_api.reports.quotes.application.port;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;

import java.util.List;


public interface QuotesReportPdfPort {
    byte[] generatePdf(List<QuoteListResponse> quotes, String mode);
}
