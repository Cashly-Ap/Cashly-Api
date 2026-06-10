package com.parsun.parsun_api.reports.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;
import com.parsun.parsun_api.reports.quotes.application.port.QuotesReportPdfPort;

import java.util.List;


public class GenerateQuotesReportPdfUseCaseImpl implements GenerateQuotesReportPdfUseCase {

    private final QuotesReportPdfPort quotesReportPdfPort;

    public GenerateQuotesReportPdfUseCaseImpl(QuotesReportPdfPort quotesReportPdfPort) {
        this.quotesReportPdfPort = quotesReportPdfPort;
    }

    @Override
    public byte[] execute(List<QuoteListResponse> quotes, String mode) {
        return quotesReportPdfPort.generatePdf(quotes, mode != null ? mode : "simple");
    }
}
