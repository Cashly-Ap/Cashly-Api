package com.cashly.cashly_api.sales.quotes.reporting.application.dto.model;

import java.util.UUID;
import java.time.LocalDate;

public record QuoteReportFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String status, UUID productId, UUID categoryId) {}
