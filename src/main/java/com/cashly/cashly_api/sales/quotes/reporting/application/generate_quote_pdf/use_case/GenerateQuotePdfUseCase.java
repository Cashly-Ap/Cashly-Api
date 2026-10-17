package com.cashly.cashly_api.sales.quotes.reporting.application.generate_quote_pdf.use_case;

import java.util.UUID;


public interface GenerateQuotePdfUseCase {
    byte[] execute(UUID quoteId);
}
