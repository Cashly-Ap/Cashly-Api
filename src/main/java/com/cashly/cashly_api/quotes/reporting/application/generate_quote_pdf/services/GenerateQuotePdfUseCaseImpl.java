package com.cashly.cashly_api.quotes.reporting.application.generate_quote_pdf.services;

import com.cashly.cashly_api.quotes.reporting.application.generate_quote_pdf.use_case.GenerateQuotePdfUseCase;

import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuotePdfPort;
import java.util.UUID;

public class GenerateQuotePdfUseCaseImpl implements GenerateQuotePdfUseCase {
    private final QuoteReportQueryGateway query;
    private final QuotePdfPort pdf;
    public GenerateQuotePdfUseCaseImpl(QuoteReportQueryGateway query, QuotePdfPort pdf) {
        this.query = query;
        this.pdf = pdf;
    }
    public byte[] execute(UUID id) { return pdf.generatePdf(query.find(id)); }
}
