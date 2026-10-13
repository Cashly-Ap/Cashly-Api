package com.cashly.cashly_api.reports.quotes.application.usecases;

import com.cashly.cashly_api.reports.quotes.application.ports.out.QuoteReportQueryGateway;
import com.cashly.cashly_api.reports.quotes.application.ports.out.QuotePdfPort;
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
