package com.cashly.cashly_api.quotes.reporting.application.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record QuoteReportRow(UUID id, String quoteNumber, String customerName, String status, BigDecimal subtotal, Instant createdAt) {}
