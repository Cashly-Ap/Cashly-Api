package com.cashly.cashly_api.quotes.reporting.application.generate_quotes_report_pdf;

import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportFilter;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuotesReportPdfPort;



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
