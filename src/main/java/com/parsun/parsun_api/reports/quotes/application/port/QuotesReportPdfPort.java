package com.parsun.parsun_api.reports.quotes.application.port;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;

import java.util.List;

/**
 * Port for generating PDF report for multiple quotes.
 */
public interface QuotesReportPdfPort {
    byte[] generatePdf(List<QuoteListResponse> quotes, String mode);
}
