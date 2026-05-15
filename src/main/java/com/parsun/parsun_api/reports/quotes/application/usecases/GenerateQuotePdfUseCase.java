package com.parsun.parsun_api.reports.quotes.application.usecases;

import java.util.UUID;

/**
 * Use case for generating PDF of a single quote.
 */
public interface GenerateQuotePdfUseCase {
    byte[] execute(UUID quoteId);
}
