package com.parsun.parsun_api.reports.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;

import java.util.List;

/**
 * Use case for generating PDF report of multiple quotes.
 */
public interface GenerateQuotesReportPdfUseCase {
    byte[] execute(List<QuoteListResponse> quotes, String mode);
}
