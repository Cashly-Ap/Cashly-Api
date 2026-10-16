package com.cashly.cashly_api.quotes.reporting.application.generate_quote_pdf;

import java.util.UUID;


public interface GenerateQuotePdfUseCase {
    byte[] execute(UUID quoteId);
}
