package com.cashly.cashly_api.sales.quotes.application.ports.in;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record QuoteDocumentRow(UUID id, String quoteNumber, String customerName,
        String status, BigDecimal subtotal, Instant createdAt) {
}
