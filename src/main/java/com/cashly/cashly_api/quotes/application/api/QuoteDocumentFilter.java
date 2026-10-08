package com.cashly.cashly_api.quotes.application.api;

import java.util.UUID;
import java.time.LocalDate;

public record QuoteDocumentFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String status, UUID productId, UUID categoryId) {}
