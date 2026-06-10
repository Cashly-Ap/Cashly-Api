package com.parsun.parsun_api.reports.quotes.application.port;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;

import java.util.List;


public interface QuotesReportPdfPort {
    byte[] generatePdf(List<QuoteListResponse> quotes, String mode);
}
