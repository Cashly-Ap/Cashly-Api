package com.cashly.cashly_api.quotes.application.convert_to_invoice.response;

import java.util.UUID;

public record QuoteConvertedToInvoiceResponse(
        UUID quoteId,
        String quoteNumber,
        UUID invoiceId,
        String invoiceNumber
) {}
