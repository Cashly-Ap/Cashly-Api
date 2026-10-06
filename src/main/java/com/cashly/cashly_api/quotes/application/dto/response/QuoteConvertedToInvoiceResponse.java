package com.cashly.cashly_api.quotes.application.dto.response;

import java.util.UUID;

public record QuoteConvertedToInvoiceResponse(
        UUID quoteId,
        String quoteNumber,
        UUID invoiceId,
        String invoiceNumber
) {}
