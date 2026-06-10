package com.parsun.parsun_api.reports.quotes.application.usecases;

import java.util.UUID;


public interface GenerateQuotePdfUseCase {
    byte[] execute(UUID quoteId);
}
