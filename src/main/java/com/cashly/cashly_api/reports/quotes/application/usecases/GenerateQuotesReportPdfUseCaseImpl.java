package com.cashly.cashly_api.reports.quotes.application.usecases;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportFilter;
import com.cashly.cashly_api.reports.quotes.application.contracts.QuoteReportQueryGateway;
import com.cashly.cashly_api.reports.quotes.application.port.QuotesReportPdfPort;



public class GenerateQuotesReportPdfUseCaseImpl implements GenerateQuotesReportPdfUseCase {

    private final QuoteReportQueryGateway query;
    private final QuotesReportPdfPort quotesReportPdfPort;

    public GenerateQuotesReportPdfUseCaseImpl(QuoteReportQueryGateway query, QuotesReportPdfPort quotesReportPdfPort) {
        this.query = query;
        this.quotesReportPdfPort = quotesReportPdfPort;
    }

    @Override
    public byte[] execute(QuoteReportFilter filter, String mode) {
        return quotesReportPdfPort.generatePdf(query.findAll(filter), mode != null ? mode : "simple");
    }
}
