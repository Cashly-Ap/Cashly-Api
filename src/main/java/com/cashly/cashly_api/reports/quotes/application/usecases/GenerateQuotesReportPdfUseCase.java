package com.cashly.cashly_api.reports.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;

import java.util.List;


public interface GenerateQuotesReportPdfUseCase {
    byte[] execute(List<QuoteListResponse> quotes, String mode);
}
